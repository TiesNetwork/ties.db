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

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.ebml.TiesDBRequestConsistency;
import com.tiesdb.protocol.v0r0.exception.TiesDBProtocolMessageException;
import com.tiesdb.protocol.v0r0.reader.ComputeRetrieveReader.ComputeRetrieve;
import com.tiesdb.protocol.v0r0.reader.EntryHeaderReader;
import com.tiesdb.protocol.v0r0.reader.FieldRetrieveReader.FieldRetrieve;
import com.tiesdb.protocol.v0r0.reader.FilterReader;
import com.tiesdb.protocol.v0r0.reader.FunctionReader.ArgumentFunction;
import com.tiesdb.protocol.v0r0.reader.FunctionReader.ArgumentReference;
import com.tiesdb.protocol.v0r0.reader.FunctionReader.ArgumentStatic;
import com.tiesdb.protocol.v0r0.reader.FunctionReader.FunctionArgument;
import com.tiesdb.protocol.v0r0.reader.ModificationEntryReader.ModificationEntry;
import com.tiesdb.protocol.v0r0.reader.ModificationRequestReader.ModificationRequest;
import com.tiesdb.protocol.v0r0.reader.Reader.Request;
import com.tiesdb.protocol.v0r0.reader.RecollectionRequestReader.RecollectionRequest;
import com.tiesdb.protocol.v0r0.reader.RecollectionRequestReader.Retrieve;
import com.tiesdb.protocol.v0r0.reader.SchemaRequestReader.SchemaRequest;
import com.tiesdb.protocol.v0r0.writer.EntryHeaderWriter.EntryHeader;
import com.tiesdb.protocol.v0r0.writer.FieldWriter.Field;
import com.tiesdb.protocol.v0r0.writer.FieldWriter.Field.HashField;
import com.tiesdb.protocol.v0r0.writer.FieldWriter.Field.ValueField;
import com.tiesdb.protocol.v0r0.writer.ModificationResponseWriter.ModificationResponse;
import com.tiesdb.protocol.v0r0.writer.ModificationResponseWriter.ModificationResult;
import com.tiesdb.protocol.v0r0.writer.ModificationResultErrorWriter.ModificationResultError;
import com.tiesdb.protocol.v0r0.writer.ModificationResultSuccessWriter.ModificationResultSuccess;
import com.tiesdb.protocol.v0r0.writer.Multiple;
import com.tiesdb.protocol.v0r0.writer.RecollectionResponseWriter.RecollectionResponse;
import com.tiesdb.protocol.v0r0.writer.RecollectionResultWriter.RecollectionResult;
import com.tiesdb.protocol.v0r0.writer.ResponseWriter;
import com.tiesdb.protocol.v0r0.writer.SchemaFieldWriter.SchemaField;
import com.tiesdb.protocol.v0r0.writer.SchemaResponseWriter.SchemaResponse;
import com.tiesdb.protocol.v0r0.writer.Writer.Response;

import network.tiesdb.handler.impl.v0r0.controller.ControllerUtil.WriteConverter;
import network.tiesdb.handler.impl.v0r0.controller.MessageController.EntryImpl;
import network.tiesdb.service.api.TiesService;
import network.tiesdb.service.scope.api.TiesEntryHeader;
import network.tiesdb.service.scope.api.TiesServiceScope;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.CountConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.PercentConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.QuorumConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeException;
import network.tiesdb.service.scope.api.TiesServiceScopeModification;
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Result.Error;
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Result.Success;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Query;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Result;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Result.Field.RawField;
import network.tiesdb.service.scope.api.TiesServiceScopeSchema;
import one.utopic.sparse.ebml.EBMLFormat;
import one.utopic.sparse.ebml.format.BytesFormat;

public class RequestHandler implements Request.Visitor<Response> {

    private static final Logger LOG = LoggerFactory.getLogger(RequestHandler.class);

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF8");

    private static final byte[] EMPTY_ARRAY = new byte[0];

    private final ResponseWriter responseWriter;
    private final TiesService service;

    public RequestHandler(TiesService service, ResponseWriter responseWriter) {
        this.service = service;
        this.responseWriter = responseWriter;
    }

    public void handle(Conversation session, Request request) throws TiesDBProtocolException {
        responseWriter.accept(session, processRequest(request));
    }

    public RecollectionResult convertRecollectionResultEntry(RecollectionRequest request, Result.Entry entry) {
        TiesEntryHeader resultHeader = entry.getEntryHeader();
        EntryHeader entryHeader = new EntryHeader() {

            @Override
            public byte[] getSigner() {
                return resultHeader.getSigner();
            }

            @Override
            public byte[] getSignature() {
                return resultHeader.getSignature();
            }

            @Override
            public String getTablespaceName() {
                return request.getTablespaceName();
            }

            @Override
            public String getTableName() {
                return request.getTableName();
            }

            @Override
            public BigInteger getEntryVersion() {
                return resultHeader.getEntryVersion();
            }

            @Override
            public Date getEntryTimestamp() {
                return resultHeader.getEntryTimestamp();
            }

            @Override
            public byte[] getEntryOldHash() {
                return resultHeader.getEntryOldHash();
            }

            @Override
            public Integer getEntryNetwork() {
                return (int) resultHeader.getEntryNetwork();
            }

            @Override
            public byte[] getEntryFldHash() {
                return resultHeader.getEntryFldHash();
            }
        };

        Multiple<Field> entryFields;
        {
            List<Result.Field> entryResultFields = entry.getEntryFields();
            entryFields = new Multiple<Field>() {
                @Override
                public Iterator<Field> iterator() {
                    return entryResultFields.stream().map(RequestHandler.this::convertResultField).iterator();
                }

                @Override
                public boolean isEmpty() {
                    return entryResultFields.isEmpty();
                }
            };
        }

        Multiple<Field> computedFields;
        {
            List<Result.Field> computedResultFields = entry.getComputedFields();
            computedFields = new Multiple<Field>() {
                @Override
                public Iterator<Field> iterator() {
                    return computedResultFields.stream().map(RequestHandler.this::convertResultField).iterator();
                }

                @Override
                public boolean isEmpty() {
                    return computedResultFields.isEmpty();
                }
            };
        }
        return new RecollectionResult() {

            @Override
            public EntryHeader getEntryHeader() {
                return entryHeader;
            }

            @Override
            public Multiple<Field> getEntryFields() {
                return entryFields;
            }

            @Override
            public Multiple<Field> getComputedFields() {
                return computedFields;
            }
        };
    }

    protected Field convertResultField(Result.Field f) {
        try {
            return f.accept(new Result.Field.Visitor<Field>() {

                @Override
                public Field on(Result.Field.HashField field) {
                    return new HashField() {

                        @Override
                        public String getName() {
                            return field.getName();
                        }

                        @Override
                        public String getType() {
                            return field.getType();
                        }

                        @Override
                        public byte[] getHash() {
                            return field.getHash();
                        }

                        @Override
                        public String toString() {
                            return "HashField [name=" + getName() + ", type=" + getType() + ", hash="
                                    + DatatypeConverter.printHexBinary(getHash()) + "]";
                        }

                    };
                }

                @Override
                public Field on(Result.Field.ValueField field) throws TiesServiceScopeException {

                    @SuppressWarnings("unchecked")
                    WriteConverter<Object> wc = (WriteConverter<Object>) ControllerUtil.writeConverterForType(field.getType());

                    return new ValueField<Object>() {

                        @Override
                        public String getName() {
                            return field.getName();
                        }

                        @Override
                        public String getType() {
                            return field.getType();
                        }

                        @Override
                        public EBMLFormat<Object> getFormat() {
                            return wc.getFormat();
                        }

                        @Override
                        public Object getValue() {
                            return wc.convert(field.getValue());
                        }

                        @Override
                        public String toString() {
                            return "ValueField [name=" + getName() + ", type=" + getType() + ", value=" + getValue() + "]";
                        }

                    };
                }

                @Override
                public Field on(RawField field) throws TiesServiceScopeException {

                    return new ValueField<byte[]>() {

                        @Override
                        public String getName() {
                            return field.getName();
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
                            return field.getRawValue();
                        }

                        @Override
                        public String toString() {
                            return "ValueField [name=" + getName() + ", type=" + getType() + ", value=" + getValue() + "]";
                        }

                    };

                }

            });
        } catch (Exception e) {
            LOG.error("Can't convert field {}", f, e);
            // FIXME Implement more robust error handling
            return new HashField() {

                @Override
                public String getName() {
                    return f.getName();
                }

                @Override
                public String getType() {
                    return "error";
                }

                @Override
                public byte[] getHash() {
                    String message = e.getMessage();
                    return null == message ? new byte[0] : message.getBytes(DEFAULT_CHARSET);
                }

                @Override
                public String toString() {
                    return "HashField [name=" + getName() + ", type=" + getType() + ", hash=" + DatatypeConverter.printHexBinary(getHash())
                            + "]";
                }

            };
        }
    }

    protected static void convertArguments(List<FunctionArgument> from, Consumer<Query.Function.Argument> c) {
        for (FunctionArgument arg : from) {
            c.accept(arg.accept(new FunctionArgument.Visitor<Query.Function.Argument>() {

                @Override
                public Query.Function.Argument on(ArgumentFunction arg) {
                    return new Query.Function.Argument.FunctionArgument() {

                        List<Argument> arguments = new LinkedList<>();
                        {
                            convertArguments(arg.getFunction().getArguments(), arguments::add);
                        }

                        @Override
                        public String getName() {
                            return arg.getFunction().getName();
                        }

                        @Override
                        public List<Argument> getArguments() {
                            return arguments;
                        }

                    };
                }

                @Override
                public Query.Function.Argument on(ArgumentReference arg) {
                    return new Query.Function.Argument.FieldArgument() {

                        @Override
                        public String getFieldName() {
                            return arg.getFieldName();
                        }

                    };
                }

                @Override
                public Query.Function.Argument on(ArgumentStatic arg) {
                    return new Query.Function.Argument.ValueArgument() {

                        @Override
                        public Object getValue() throws TiesServiceScopeException {
                            return ControllerUtil.readerForType(getType()).apply(getRawValue());
                        }

                        @Override
                        public String getType() {
                            return arg.getType();
                        }

                        @Override
                        public byte[] getRawValue() {
                            return arg.getRawValue();
                        }
                    };
                }
            }));
        }
    }

    private ActionConsistency getActionConsistency(TiesDBRequestConsistency consistency) throws TiesServiceScopeException {
        switch (consistency.getType()) {
        case COUNT:
            return new CountConsistency() {
                @Override
                public Integer getValue() {
                    return consistency.getValue();
                }
            };
        case PERCENT:
            return new PercentConsistency() {
                @Override
                public Integer getValue() {
                    return consistency.getValue();
                }
            };
        case QUORUM:
            return new QuorumConsistency() {
            };
        default:
            throw new TiesServiceScopeException("Unrecognized request consistency level " + consistency);
        }
    }

    @Override
    public ModificationResponse on(ModificationRequest request) throws TiesDBProtocolMessageException {
        requireNonNull(request);

        BigInteger messageId = request.getMessageId();
        LOG.debug("MessageID: {}", messageId);

        ActionConsistency consistency;
        try {
            consistency = getActionConsistency(request.getConsistency());
        } catch (TiesServiceScopeException e) {
            throw new TiesDBProtocolMessageException(messageId, e);
        }

        TiesServiceScope serviceScope = service.newServiceScope();
        LinkedList<TiesServiceScopeModification.Result> results = new LinkedList<>();
        for (ModificationEntry modificationEntry : request.getEntries()) {
            EntryHeaderReader.EntryHeader header = modificationEntry.getHeader();
            if (null == header) {
                IllegalArgumentException e = new IllegalArgumentException("No header");
                LOG.error("Error handling ModificationRequest.Entry {}", modificationEntry, e);
                results.add(new TiesServiceScopeModification.Result.Error() {

                    @Override
                    public Throwable getError() {
                        return e;
                    }

                    @Override
                    public byte[] getHeaderHash() {
                        return EMPTY_ARRAY;
                    }

                });
                continue;
            }
            try {
                if (null == header.getEntryOldHash() && BigInteger.ONE.equals(header.getEntryVersion())) {
                    serviceScope.insert(new TiesServiceScopeModification() {

                        private final EntryImpl entry = new EntryImpl(modificationEntry, true);

                        @Override
                        public Entry getEntry() {
                            return entry;
                        }

                        @Override
                        public ActionConsistency getConsistency() {
                            return consistency;
                        }

                        @Override
                        public BigInteger getMessageId() {
                            return messageId;
                        }

                        @Override
                        public void setResult(Result result) throws TiesServiceScopeException {
                            results.add(result);
                        }

                    });
                } else if (null != header.getEntryOldHash() && BigInteger.ZERO.equals(header.getEntryVersion())) {
                    serviceScope.delete(new TiesServiceScopeModification() {

                        private final EntryImpl entry = new EntryImpl(modificationEntry, false);

                        @Override
                        public Entry getEntry() {
                            return entry;
                        }

                        @Override
                        public ActionConsistency getConsistency() {
                            return consistency;
                        }

                        @Override
                        public BigInteger getMessageId() {
                            return messageId;
                        }

                        @Override
                        public void setResult(Result result) throws TiesServiceScopeException {
                            results.add(result);
                        }

                    });
                } else if (null != header.getEntryOldHash()) {
                    serviceScope.update(new TiesServiceScopeModification() {

                        private final EntryImpl entry = new EntryImpl(modificationEntry, false);

                        @Override
                        public Entry getEntry() {
                            return entry;
                        }

                        @Override
                        public ActionConsistency getConsistency() {
                            return consistency;
                        }

                        @Override
                        public BigInteger getMessageId() {
                            return messageId;
                        }

                        @Override
                        public void setResult(Result result) throws TiesServiceScopeException {
                            results.add(result);
                        }

                    });
                } else {
                    throw new TiesServiceScopeException("Illegal modification EntryOldHash and/or EntryVersion");
                }
            } catch (TiesServiceScopeException e) {
                LOG.error("Error handling ModificationRequest.Entry {}", modificationEntry, e);
                results.add(new TiesServiceScopeModification.Result.Error() {

                    @Override
                    public Throwable getError() {
                        return e;
                    }

                    @Override
                    public byte[] getHeaderHash() {
                        return modificationEntry.getHeader().getHash();
                    }
                });
                continue;
            }
        }
        final List<ModificationResult> resultList = Collections.unmodifiableList(results.stream().map(r -> {
            try {
                return r.accept(new TiesServiceScopeModification.Result.Visitor<ModificationResult>() {
                    @Override
                    public ModificationResult on(Success success) throws TiesServiceScopeException {
                        return new ModificationResultSuccess() {
                            @Override
                            public byte[] getEntryHeaderHash() {
                                return success.getHeaderHash();
                            }
                        };
                    }

                    @Override
                    public ModificationResult on(Error error) throws TiesServiceScopeException {
                        return new ModificationResultError() {
                            @Override
                            public byte[] getEntryHeaderHash() {
                                return error.getHeaderHash();
                            }

                            @Override
                            public Throwable getError() {
                                return error.getError();
                            }
                        };
                    }
                });
            } catch (TiesServiceScopeException e) {
                return new ModificationResultError() {
                    @Override
                    public byte[] getEntryHeaderHash() {
                        return EMPTY_ARRAY;
                    }

                    @Override
                    public Throwable getError() {
                        return e;
                    }
                };
            }
        }).collect(Collectors.toList()));
        return new ModificationResponse() {

            @Override
            public BigInteger getMessageId() {
                return messageId;
            }

            @Override
            public Iterable<ModificationResult> getResults() {
                return resultList;
            }

        };
    }

    @Override
    public Response on(RecollectionRequest request) throws TiesDBProtocolMessageException {
        requireNonNull(request);

        BigInteger messageId = request.getMessageId();
        LOG.debug("MessageID: {}", messageId);

        ActionConsistency consistency;
        try {
            consistency = getActionConsistency(request.getConsistency());
        } catch (TiesServiceScopeException e) {
            throw new TiesDBProtocolMessageException(messageId, e);
        }

        TiesServiceScope serviceScope = service.newServiceScope();
        List<RecollectionResult> results = new LinkedList<>();
        try {
            serviceScope.select(new TiesServiceScopeRecollection() {

                private final List<Query.Selector> selectors = new LinkedList<>();
                {
                    for (Retrieve r : request.getRetrieves()) {
                        selectors.add(r.accept(new Retrieve.Visitor<Query.Selector>() {

                            @Override
                            public Query.Selector on(FieldRetrieve retrieve) {
                                return new Query.Selector.FieldSelector() {
                                    @Override
                                    public String getFieldName() {
                                        return retrieve.getFieldName();
                                    }
                                };
                            }

                            @Override
                            public Query.Selector on(ComputeRetrieve retrieve) {
                                return new Query.Selector.FunctionSelector() {

                                    List<Argument> arguments = new LinkedList<>();
                                    {
                                        convertArguments(retrieve.getArguments(), arguments::add);
                                    }

                                    @Override
                                    public String getName() {
                                        return retrieve.getName();
                                    }

                                    @Override
                                    public List<Argument> getArguments() {
                                        return arguments;
                                    }

                                    @Override
                                    public String getAlias() {
                                        return retrieve.getAlias();
                                    }

                                    @Override
                                    public String getType() {
                                        return retrieve.getType();
                                    }
                                };
                            }
                        }));
                    }
                }
                private final List<Query.Filter> filters = new LinkedList<>();
                {
                    for (FilterReader.Filter filter : request.getFilters()) {
                        filters.add(new Query.Filter() {

                            private final List<Argument> arguments = new LinkedList<>();
                            {
                                convertArguments(filter.getArguments(), arguments::add);
                            }

                            @Override
                            public String getName() {
                                return filter.getName();
                            }

                            @Override
                            public List<Argument> getArguments() {
                                return arguments;
                            }

                            @Override
                            public String getFieldName() {
                                return filter.getFieldName();
                            }
                        });
                    }
                }

                @Override
                public Query getQuery() {
                    return new Query() {

                        @Override
                        public String getTablespaceName() {
                            return request.getTablespaceName();
                        }

                        @Override
                        public String getTableName() {
                            return request.getTableName();
                        }

                        @Override
                        public List<Selector> getSelectors() {
                            return selectors;
                        }

                        @Override
                        public List<Filter> getFilters() {
                            return filters;
                        }
                    };
                }

                @Override
                public void setResult(Result r) throws TiesServiceScopeException {
                    LOG.debug("AddedResult {}", r);
                    r.getEntries().stream().map(entry -> convertRecollectionResultEntry(request, entry)).forEach(results::add);
                }

                @Override
                public ActionConsistency getConsistency() {
                    return consistency;
                }

                @Override
                public BigInteger getMessageId() {
                    return messageId;
                }

            });

            return new RecollectionResponse() {

                @Override
                public BigInteger getMessageId() {
                    return messageId;
                }

                @Override
                public Iterable<RecollectionResult> getResults() {
                    return results;
                }

            };

        } catch (TiesServiceScopeException e) {
            LOG.error("Error handling RecollectionRequest {}", request, e);
            throw new TiesDBProtocolMessageException(messageId, "Error handling RecollectionRequest", e);
        }

    }

    @Override
    public Response on(SchemaRequest request) throws TiesDBProtocolException {
        requireNonNull(request);

        BigInteger messageId = request.getMessageId();
        LOG.debug("MessageID: {}", messageId);

        // ActionConsistency consistency;
        // try {
        // consistency = getActionConsistency(request.getConsistency());
        // } catch (TiesServiceScopeException e) {
        // throw new TiesDBProtocolMessageException(messageId, e);
        // }

        TiesServiceScope serviceScope = service.newServiceScope();
        LinkedList<SchemaField> schemaFields = new LinkedList<>();
        try {
            serviceScope.schema(new TiesServiceScopeSchema() {

                @Override
                public String getTablespaceName() {
                    return request.getTablespaceName();
                }

                @Override
                public String getTableName() {
                    return request.getTableName();
                }

                @Override
                public BigInteger getMessageId() {
                    return messageId;
                }

                @Override
                public void setResult(FieldSchema fieldSchema) throws TiesServiceScopeException {
                    fieldSchema.getFields().forEach(field -> {
                        schemaFields.add(new SchemaField() {

                            @Override
                            public String getType() {
                                return field.getFieldType();
                            }

                            @Override
                            public String getName() {
                                return field.getFieldName();
                            }

                            @Override
                            public boolean isPrimary() {
                                return field.isPrimary();
                            }

                            @Override
                            public String toString() {
                                return "SchemaField [" + (isPrimary() ? "primary, " : "") + "name=" + getName() + ", type=" + getType()
                                        + "]";
                            }

                        });
                    });
                }
            });
        } catch (TiesServiceScopeException e) {
            LOG.error("Error handling SchemaRequest {}", request, e);
            throw new TiesDBProtocolMessageException(messageId, "Error handling SchemaRequest", e);
        }

        return new SchemaResponse() {

            @Override
            public BigInteger getMessageId() {
                return messageId;
            }

            @Override
            public Iterable<SchemaField> getFields() {
                return schemaFields;
            }

        };
    }

    protected Response processRequest(Request request) throws TiesDBProtocolException {
        if (null == request) {
            throw new TiesDBProtocolException("Empty request");
        }
        LOG.debug("Request: {}", request);
        if (null == request.getMessageId()) {
            throw new TiesDBProtocolException("Request MessageId is required");
        }
        try {
            return request.accept(this);
        } catch (TiesDBProtocolMessageException e) {
            throw e;
        } catch (Exception e) {
            throw new TiesDBProtocolMessageException(request.getMessageId(), e);
        }
    }
}
