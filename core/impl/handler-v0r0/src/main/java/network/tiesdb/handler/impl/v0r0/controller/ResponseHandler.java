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

import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.exception.TiesDBProtocolMessageException;
import com.tiesdb.protocol.v0r0.reader.EntryHeaderReader.EntryHeader;
import com.tiesdb.protocol.v0r0.reader.FieldReader;
import com.tiesdb.protocol.v0r0.reader.ModificationResponseReader;
import com.tiesdb.protocol.v0r0.reader.ModificationResponseReader.ModificationResponse;
import com.tiesdb.protocol.v0r0.reader.ModificationResponseReader.ModificationResult;
import com.tiesdb.protocol.v0r0.reader.ModificationResultErrorReader.ModificationResultError;
import com.tiesdb.protocol.v0r0.reader.ModificationResultSuccessReader.ModificationResultSuccess;
import com.tiesdb.protocol.v0r0.reader.Reader.Response;
import com.tiesdb.protocol.v0r0.reader.RecollectionResponseReader.RecollectionResponse;
import com.tiesdb.protocol.v0r0.reader.SchemaResponseReader.SchemaResponse;

import network.tiesdb.exception.TiesException;
import network.tiesdb.service.api.TiesService;
import network.tiesdb.service.scope.api.TiesEntryHeader;
import network.tiesdb.service.scope.api.TiesServiceScope;
import network.tiesdb.service.scope.api.TiesServiceScopeException;
import network.tiesdb.service.scope.api.TiesServiceScopeModification;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Result.Field;
import network.tiesdb.service.scope.api.TiesServiceScopeResult;

public class ResponseHandler implements Response.Visitor<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseHandler.class);

    private final TiesService service;

    public ResponseHandler(TiesService service) {
        this.service = service;
    }

    public void handle(Conversation session, Response response) throws TiesDBProtocolException {
        if (null == response) {
            throw new TiesDBProtocolException("Empty response");
        }
        LOG.debug("Response: {}", response);
        if (null == response.getMessageId()) {
            throw new TiesDBProtocolException("Response MessageId is required");
        }
        try {
            response.accept(this);
        } catch (TiesDBProtocolMessageException e) {
            throw e;
        } catch (Exception e) {
            throw new TiesDBProtocolMessageException(response.getMessageId(), e);
        }
    }

    @Override
    public Void on(ModificationResponse modificationResponse) throws TiesDBProtocolException {

        BigInteger messageId = modificationResponse.getMessageId();

        TiesServiceScope serviceScope = service.newServiceScope();
        for (ModificationResult modificationResult : modificationResponse.getModificationResults()) {
            try {
                TiesServiceScopeResult.Result result = modificationResult
                        .accept(new ModificationResponseReader.ModificationResult.Visitor<TiesServiceScopeResult.Result>() {
                            @Override
                            public TiesServiceScopeResult.Result on(ModificationResultSuccess modificationResultSuccess) {
                                return new TiesServiceScopeModification.Result.Success() {
                                    @Override
                                    public byte[] getHeaderHash() {
                                        return modificationResultSuccess.getEntryHeaderHash();
                                    }
                                };
                            }

                            @Override
                            public TiesServiceScopeResult.Result on(ModificationResultError modificationResultError) {
                                return new TiesServiceScopeModification.Result.Error() {

                                    @Override
                                    public byte[] getHeaderHash() {
                                        return modificationResultError.getEntryHeaderHash();
                                    }

                                    @Override
                                    public Throwable getError() {
                                        return new TiesException(modificationResultError.getMessage());
                                    }
                                };
                            }
                        });
                serviceScope.result(new TiesServiceScopeResult() {

                    @Override
                    public BigInteger getMessageId() {
                        return messageId;
                    }

                    @Override
                    public Result getResult() {
                        return result;
                    }

                });
            } catch (TiesServiceScopeException e) {
                throw new TiesDBProtocolException("Response handling failed", e);
            }
        }
        return null;
    }

    @Override
    public Void on(RecollectionResponse recollectionResponse) throws TiesDBProtocolException {

        BigInteger messageId = recollectionResponse.getMessageId();

        TiesServiceScope serviceScope = service.newServiceScope();
        try {
            TiesServiceScopeResult.Result result = new TiesServiceScopeRecollection.Result() {

                private final List<Entry> entries;
                {
                    CompletableFuture<List<Entry>> entriesFuture = CompletableFuture.supplyAsync(() -> {
                        return recollectionResponse.getRecollectionResults().parallelStream().map(r -> {
                            return new Entry() {

                                private final TiesEntryHeader header = convertHeader(r.getHeader());
                                private final List<Field> entryFields = convertFields(r.getFields().values());
                                private final List<Field> computedFields = convertFields(r.getComputeFields());

                                @Override
                                public TiesEntryHeader getEntryHeader() {
                                    return header;
                                }

                                @Override
                                public List<Field> getEntryFields() {
                                    return entryFields;
                                }

                                @Override
                                public List<Field> getComputedFields() {
                                    return computedFields;
                                }

                            };
                        }).collect(Collectors.toList());
                    });
                    entries = entriesFuture.get();
                }

                @Override
                public List<Entry> getEntries() {
                    return entries;
                }

            };
            serviceScope.result(new TiesServiceScopeResult() {

                @Override
                public BigInteger getMessageId() {
                    return messageId;
                }

                @Override
                public Result getResult() {
                    return result;
                }

            });
        } catch (InterruptedException | ExecutionException | TiesServiceScopeException e) {
            throw new TiesDBProtocolException("Response handling failed", e);
        }
        return null;
    }

    @Override
    public Void on(SchemaResponse schemaResponse) throws TiesDBProtocolException {
        // TODO Auto-generated method stub
        throw new TiesDBProtocolException("Not implemented");
    }

    private static TiesEntryHeader convertHeader(EntryHeader header) {
        return new TiesEntryHeader() {

            @Override
            public byte[] getSigner() {
                return header.getSigner();
            }

            @Override
            public byte[] getSignature() {
                return header.getSignature();
            }

            @Override
            public byte[] getHash() {
                return header.getHash();
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
            public short getEntryNetwork() {
                return header.getEntryNetwork().shortValue();
            }

            @Override
            public byte[] getEntryFldHash() {
                return header.getEntryFldHash();
            }
        };
    }

    private List<TiesServiceScopeRecollection.Result.Field> convertFields(Collection<FieldReader.Field> fields) {
        List<Field> result = fields.parallelStream().map(field -> {
            byte[] raw = field.getRawValue();
            if (null != raw) {
                return new TiesServiceScopeRecollection.Result.Field.RawField() {

                    @Override
                    public String getType() {
                        return field.getType();
                    }

                    @Override
                    public String getName() {
                        return field.getName();
                    }

                    @Override
                    public byte[] getRawValue() {
                        return raw;
                    }
                };
            } else {
                return new TiesServiceScopeRecollection.Result.Field.HashField() {

                    @Override
                    public String getType() {
                        return field.getType();
                    }

                    @Override
                    public String getName() {
                        return field.getName();
                    }

                    @Override
                    public byte[] getHash() {
                        return field.getHash();
                    }
                };
            }
        }).collect(Collectors.toList());
        result.sort((a, b) -> {
            return compareStrings(a.getName(), b.getName());
        });
        return result;
    }

    private static int compareStrings(String a, String b) {
        if (a == b) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        return a.compareTo(b);
    }
}
