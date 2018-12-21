/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package network.tiesdb.handler.impl.v0r0.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.ebml.TiesDBRequestConsistency;
import com.tiesdb.protocol.v0r0.ebml.TiesDBRequestConsistency.ConsistencyType;
import com.tiesdb.protocol.v0r0.writer.EntryHeaderWriter.EntryHeader;
import com.tiesdb.protocol.v0r0.writer.FieldWriter.Field;
import com.tiesdb.protocol.v0r0.writer.ModificationEntryWriter.ModificationEntry;
import com.tiesdb.protocol.v0r0.writer.ModificationRequestWriter.ModificationRequest;
import com.tiesdb.protocol.v0r0.writer.RecollectionRequestWriter.RecollectionRequest;
import com.tiesdb.protocol.v0r0.writer.RequestWriter;
import com.tiesdb.protocol.v0r0.writer.AbstractFunctionWriter.Function;
import com.tiesdb.protocol.v0r0.writer.AbstractFunctionWriter.Function.Argument;

import network.tiesdb.api.TiesVersion;
import network.tiesdb.service.api.TiesService;
import network.tiesdb.service.scope.api.TiesEntryHeader;
import network.tiesdb.service.scope.api.TiesServiceScope;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.CountConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.PercentConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.QuorumConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeResult;
import network.tiesdb.service.scope.api.TiesServiceScopeException;
import network.tiesdb.service.scope.api.TiesServiceScopeModification;
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Entry;
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Entry.FieldHash;
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Entry.FieldValue;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Query;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Query.Function.Argument.FieldArgument;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Query.Function.Argument.FunctionArgument;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Query.Function.Argument.ValueArgument;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Query.Selector.FieldSelector;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Query.Selector.FunctionSelector;
import network.tiesdb.service.scope.api.TiesServiceScopeSchema;
import one.utopic.sparse.ebml.EBMLFormat;
import one.utopic.sparse.ebml.format.BytesFormat;

public class ServiceClientController implements TiesServiceScope {

    private static final RequestWriter REQUEST_WRITER_INSTANCE = new RequestWriter();

    private static final ActionConsistency.Visitor<TiesDBRequestConsistency> CONSISTENCY_SELECTOR = new ActionConsistency.Visitor<TiesDBRequestConsistency>() {

        @Override
        public TiesDBRequestConsistency on(CountConsistency countConsistency) {
            return new TiesDBRequestConsistency(ConsistencyType.COUNT, countConsistency.getValue());
        }

        @Override
        public TiesDBRequestConsistency on(PercentConsistency percentConsistency) {
            return new TiesDBRequestConsistency(ConsistencyType.PERCENT, percentConsistency.getValue());
        }

        @Override
        public TiesDBRequestConsistency on(QuorumConsistency quorumConsistency) {
            return new TiesDBRequestConsistency(ConsistencyType.QUORUM, 0);
        }

    };

    private static final TiesDBRequestConsistency DEFAULT_CONSISTENCY = new TiesDBRequestConsistency(ConsistencyType.QUORUM, 0);

    private final Conversation session;
    private final TiesService service;

    public ServiceClientController(TiesService service, Conversation session) {
        this.service = service;
        this.session = session;
    }

    @Override
    public void close() throws IOException {
        this.session.close();
    }

    @Override
    public void insert(TiesServiceScopeModification action) throws TiesServiceScopeException {
        modify(action);
    }

    @Override
    public void update(TiesServiceScopeModification action) throws TiesServiceScopeException {
        modify(action);
    }

    @Override
    public void delete(TiesServiceScopeModification action) throws TiesServiceScopeException {
        modify(action);
    }

    private void modify(TiesServiceScopeModification action) throws TiesServiceScopeException {
        try {
            Entry entry = action.getEntry();
            if (null == entry) {
                throw new TiesServiceScopeException("No entry found in modification request");
            }
            TiesEntryHeader entryHeader = entry.getHeader();
            if (null == entryHeader) {
                throw new TiesServiceScopeException("No header found in modification request entry");
            }
            REQUEST_WRITER_INSTANCE.accept(session, new ModificationRequest() {

                @Override
                public TiesDBRequestConsistency getConsistency() {
                    return convertConsistency(action.getConsistency());
                }

                @Override
                public Iterable<ModificationEntry> getEntries() {
                    return Arrays.asList(new ModificationEntry() {

                        private final Iterable<Field> fields;
                        {
                            Map<String, FieldHash> fieldHashes = entry.getFieldHashes();
                            Map<String, FieldValue> fieldValues = entry.getFieldValues();
                            HashSet<String> fieldNames = new HashSet<>(fieldHashes.size() + fieldValues.size());
                            fieldNames.addAll(fieldHashes.keySet());
                            fieldNames.addAll(fieldValues.keySet());
                            LinkedList<Field> fieldsCache = new LinkedList<>();
                            for (final String name : fieldNames) {
                                {
                                    FieldValue field = fieldValues.get(name);
                                    if (null != field) {
                                        fieldsCache.add(new Field.ValueField<byte[]>() {

                                            @Override
                                            public String getName() {
                                                return name;
                                            }

                                            @Override
                                            public String getType() {
                                                return field.getType();
                                            }

                                            @Override
                                            public EBMLFormat<byte[]> getFormat() {
                                                return BytesFormat.INSTANCE;
                                            }

                                            @Override
                                            public byte[] getValue() {
                                                return field.getBytes();
                                            }
                                        });
                                        continue;
                                    }
                                }
                                {
                                    FieldHash field = fieldHashes.get(name);
                                    if (null != field) {
                                        fieldsCache.add(new Field.HashField() {

                                            @Override
                                            public String getName() {
                                                return name;
                                            }

                                            @Override
                                            public String getType() {
                                                return field.getType();
                                            }

                                            @Override
                                            public byte[] getHash() {
                                                return field.getHash();
                                            }
                                        });
                                        continue;
                                    }
                                }
                            }
                            fields = Collections.unmodifiableList(fieldsCache);
                        }

                        @Override
                        public Iterable<Field> getFields() {
                            return fields;
                        }

                        @Override
                        public EntryHeader getHeader() {
                            return new EntryHeader() {

                                TiesEntryHeader header = entry.getHeader();

                                @Override
                                public byte[] getSigner() {
                                    return header.getSigner();
                                }

                                @Override
                                public byte[] getSignature() {
                                    return header.getSignature();
                                }

                                @Override
                                public String getTablespaceName() {
                                    return entry.getTablespaceName();
                                }

                                @Override
                                public String getTableName() {
                                    return entry.getTableName();
                                }

                                @Override
                                public BigInteger getEntryVersion() {
                                    return header.getEntryVersion();
                                }

                                @Override
                                public Date getEntryTimestamp() {
                                    return header.getEntryTimestamp();
                                }

                                @Override
                                public byte[] getEntryOldHash() {
                                    return header.getEntryOldHash();
                                }

                                @Override
                                public Integer getEntryNetwork() {
                                    return Short.toUnsignedInt(header.getEntryNetwork());
                                }

                                @Override
                                public byte[] getEntryFldHash() {
                                    return header.getEntryFldHash();
                                }
                            };
                        }

                    });
                }

                @Override
                public BigInteger getMessageId() {
                    return action.getMessageId();
                }

            });
            action.setResult(new TiesServiceScopeModification.Result.Success() {
                @Override
                public byte[] getHeaderHash() {
                    return entryHeader.getHash();
                }
            });
        } catch (TiesDBProtocolException e) {
            throw new TiesServiceScopeException("Node modification request failed", e);
        }
    }

    @Override
    public void select(TiesServiceScopeRecollection action) throws TiesServiceScopeException {
        Query query = action.getQuery();
        if (null == query) {
            throw new TiesServiceScopeException("No query found in recollection request");
        }
        try {
            try {
                REQUEST_WRITER_INSTANCE.accept(session, new RecollectionRequest() {

                    private final List<Retrieve> retrieves;
                    private final List<Filter> filters;
                    {
                        CompletableFuture<List<Retrieve>> retrievesFuture = CompletableFuture.supplyAsync(() -> {
                            return query.getSelectors().parallelStream().map(selector -> {
                                try {
                                    return selector.accept(new Query.Selector.Visitor<RecollectionRequest.Retrieve>() {

                                        @Override
                                        public RecollectionRequest.Retrieve on(FieldSelector fieldSelector)
                                                throws TiesServiceScopeException {
                                            return new RecollectionRequest.Retrieve.Field() {
                                                @Override
                                                public String getFieldName() {
                                                    return fieldSelector.getFieldName();
                                                }
                                            };
                                        }

                                        @Override
                                        public RecollectionRequest.Retrieve on(FunctionSelector functionSelector)
                                                throws TiesServiceScopeException {
                                            return new RecollectionRequest.Retrieve.Compute() {

                                                private final List<Argument> arguments = convertFunctionArguments(
                                                        functionSelector.getArguments());

                                                @Override
                                                public String getName() {
                                                    return functionSelector.getName();
                                                }

                                                @Override
                                                public List<Argument> getArguments() {
                                                    return arguments;
                                                }

                                                @Override
                                                public String getType() {
                                                    return functionSelector.getType();
                                                }

                                                @Override
                                                public String getAlias() {
                                                    return functionSelector.getAlias();
                                                }
                                            };
                                        }
                                    });
                                } catch (TiesServiceScopeException e) {
                                    throw new ConversionException("Entyty async conversion failed", e);
                                }
                            }).collect(Collectors.toList());
                        });
                        CompletableFuture<List<Filter>> filtersFuture = CompletableFuture.supplyAsync(() -> {
                            return query.getFilters().stream().map(filter -> {
                                return new RecollectionRequest.Filter() {

                                    private final List<Argument> arguments = convertFunctionArguments(filter.getArguments());

                                    @Override
                                    public String getName() {
                                        return filter.getName();
                                    }

                                    @Override
                                    public String getFieldName() {
                                        return filter.getFieldName();
                                    }

                                    @Override
                                    public List<Argument> getArguments() {
                                        return arguments;
                                    }
                                };
                            }).collect(Collectors.toList());
                        });
                        retrieves = retrievesFuture.get();
                        filters = filtersFuture.get();
                    }

                    @Override
                    public BigInteger getMessageId() {
                        return action.getMessageId();
                    }

                    @Override
                    public String getTablespaceName() {
                        return query.getTablespaceName();
                    }

                    @Override
                    public String getTableName() {
                        return query.getTableName();
                    }

                    @Override
                    public List<Retrieve> getRetrieves() {
                        return retrieves;
                    }

                    @Override
                    public List<Filter> getFilters() {
                        return filters;
                    }

                    @Override
                    public TiesDBRequestConsistency getConsistency() {
                        return convertConsistency(action.getConsistency());
                    }

                });
            } catch (ConversionException | InterruptedException | ExecutionException e) {
                throw new TiesDBProtocolException("Entyty async conversion failed", e);
            }
        } catch (TiesDBProtocolException e) {
            throw new TiesServiceScopeException("Node modification request failed", e);
        }
    }

    @Override
    public void schema(TiesServiceScopeSchema query) throws TiesServiceScopeException {
        // TODO Auto-generated method stub
        throw new TiesServiceScopeException("Not implemented");
    }

    @Override
    public TiesVersion getServiceVersion() {
        return service.getVersion();
    }

    @Override
    public void result(TiesServiceScopeResult result) throws TiesServiceScopeException {
        throw new TiesServiceScopeException("Client should not handle any result");
    }

    private static final List<Function.Argument> convertFunctionArguments(List<Query.Function.Argument> list) throws ConversionException {
        return list.parallelStream().map(arg -> {
            try {
                return arg.accept(new Query.Function.Argument.Visitor<Function.Argument>() {

                    @Override
                    public Argument on(FunctionArgument a) throws TiesServiceScopeException {
                        return new Function.Argument.ArgumentFunction() {

                            @Override
                            public String getName() {
                                return a.getName();
                            }

                            @Override
                            public List<Argument> getArguments() {
                                return convertFunctionArguments(a.getArguments());
                            }
                        };
                    }

                    @Override
                    public Argument on(ValueArgument a) throws TiesServiceScopeException {
                        return new Function.Argument.ArgumentStatic() {

                            @Override
                            public String getType() {
                                return a.getType();
                            }

                            @Override
                            public byte[] getRawValue() {
                                return a.getRawValue();
                            }
                        };
                    }

                    @Override
                    public Argument on(FieldArgument a) throws TiesServiceScopeException {
                        return new Function.Argument.ArgumentReference() {

                            @Override
                            public String getName() {
                                return a.getFieldName();
                            }
                        };
                    }
                });
            } catch (TiesServiceScopeException e) {
                throw new ConversionException(e);
            }
        }).collect(Collectors.toList());
    }

    private static TiesDBRequestConsistency convertConsistency(ActionConsistency consistency) {
        if (null == consistency) {
            return DEFAULT_CONSISTENCY;
        }
        return consistency.accept(CONSISTENCY_SELECTOR);
    }

}
