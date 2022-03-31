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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.exception.TiesDBProtocolMessageException;
import com.tiesdb.protocol.v0r0.reader.EntryHeaderReader.EntryHeader;
import com.tiesdb.protocol.v0r0.reader.FieldReader;
import com.tiesdb.protocol.v0r0.reader.HealingResponseReader;
import com.tiesdb.protocol.v0r0.reader.HealingResponseReader.HealingResponse;
import com.tiesdb.protocol.v0r0.reader.HealingResponseReader.HealingResult;
import com.tiesdb.protocol.v0r0.reader.HealingResultErrorReader.HealingResultError;
import com.tiesdb.protocol.v0r0.reader.HealingResultSuccessReader.HealingResultSuccess;
import com.tiesdb.protocol.v0r0.reader.ModificationResponseReader;
import com.tiesdb.protocol.v0r0.reader.ModificationResponseReader.ModificationResponse;
import com.tiesdb.protocol.v0r0.reader.ModificationResponseReader.ModificationResult;
import com.tiesdb.protocol.v0r0.reader.ModificationResultErrorReader.ModificationResultError;
import com.tiesdb.protocol.v0r0.reader.ModificationResultSuccessReader.ModificationResultSuccess;
import com.tiesdb.protocol.v0r0.reader.Reader.Response;
import com.tiesdb.protocol.v0r0.reader.RecollectionErrorReader.RecollectionError;
import com.tiesdb.protocol.v0r0.reader.RecollectionResponseReader;
import com.tiesdb.protocol.v0r0.reader.RecollectionResponseReader.RecollectionResponse;
import com.tiesdb.protocol.v0r0.reader.RecollectionResponseReader.RecollectionResult;
import com.tiesdb.protocol.v0r0.reader.RecollectionResultReader.RecollectionEntry;
import com.tiesdb.protocol.v0r0.reader.SchemaResponseReader.SchemaResponse;

import network.tiesdb.exception.TiesException;
import network.tiesdb.service.api.TiesService;
import network.tiesdb.service.scope.api.TiesEntryHeader;
import network.tiesdb.service.scope.api.TiesServiceScope;
import network.tiesdb.service.scope.api.TiesServiceScopeException;
import network.tiesdb.service.scope.api.TiesServiceScopeHealingAction;
import network.tiesdb.service.scope.api.TiesServiceScopeModificationAction;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollectionAction;
import network.tiesdb.service.scope.api.TiesServiceScopeResultAction;

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

        TiesServiceScope serviceScope;
        try {
            serviceScope = service.newServiceScope();
        } catch (TiesServiceScopeException e) {
            throw new TiesDBProtocolException("Response could not be handled", e);
        }
        for (ModificationResult modificationResult : modificationResponse.getModificationResults()) {
            try {
                TiesServiceScopeResultAction.Result result = modificationResult
                        .accept(new ModificationResponseReader.ModificationResult.Visitor<TiesServiceScopeResultAction.Result>() {
                            @Override
                            public TiesServiceScopeResultAction.Result on(ModificationResultSuccess modificationResultSuccess) {
                                return new TiesServiceScopeModificationAction.Result.Success() {
                                    @Override
                                    public byte[] getHeaderHash() {
                                        return modificationResultSuccess.getEntryHeaderHash();
                                    }
                                };
                            }

                            @Override
                            public TiesServiceScopeResultAction.Result on(ModificationResultError modificationResultError) {
                                return new TiesServiceScopeModificationAction.Result.Error() {

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
                serviceScope.result(new TiesServiceScopeResultAction() {

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

        TiesServiceScope serviceScope;
        try {
            serviceScope = service.newServiceScope();
        } catch (TiesServiceScopeException e) {
            throw new TiesDBProtocolException("Response could not be handled", e);
        }
        try {
            TiesServiceScopeResultAction.Result result = new TiesServiceScopeRecollectionAction.Partial() {

                private final List<TiesServiceScopeRecollectionAction.Result.Entry> entries;
                private final List<Throwable> errors;

                {
                    List<TiesServiceScopeRecollectionAction.Result.Entry> ent = new LinkedList<>();
                    List<Throwable> err = new LinkedList<>();
                    for (RecollectionResult r : recollectionResponse.getRecollectionResults()) {
                        r.accept(new RecollectionResponseReader.RecollectionResult.Visitor<Void>() {

                            @Override
                            public Void on(RecollectionEntry entry) {
                                ent.add(new TiesServiceScopeRecollectionAction.Result.Entry() {

                                    private final TiesEntryHeader header = convertHeader(entry.getHeader());
                                    private final List<TiesServiceScopeRecollectionAction.Result.Field> entryFields = convertFields(
                                            entry.getFields().values());
                                    private final List<TiesServiceScopeRecollectionAction.Result.Field> computedFields = convertFields(
                                            entry.getComputeFields());

                                    @Override
                                    public TiesEntryHeader getEntryHeader() {
                                        return header;
                                    }

                                    @Override
                                    public List<TiesServiceScopeRecollectionAction.Result.Field> getEntryFields() {
                                        return entryFields;
                                    }

                                    @Override
                                    public List<TiesServiceScopeRecollectionAction.Result.Field> getComputedFields() {
                                        return computedFields;
                                    }

                                });
                                return null;
                            }

                            @Override
                            public Void on(RecollectionError error) {
                                err.add(new Throwable(error.getMessage()));
                                return null;
                            }
                        });
                    }
                    entries = Collections.unmodifiableList(ent);
                    errors = Collections.unmodifiableList(err);
                }

                @Override
                public List<Entry> getEntries() {
                    return entries;
                }

                @Override
                public List<Throwable> getErrors() {
                    return errors;
                }

            };
            serviceScope.result(new TiesServiceScopeResultAction() {

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
        return null;
    }

    @Override
    public Void on(HealingResponse healingResponse) throws TiesDBProtocolException {

        BigInteger messageId = healingResponse.getMessageId();

        TiesServiceScope serviceScope;
        try {
            serviceScope = service.newServiceScope();
        } catch (TiesServiceScopeException e) {
            throw new TiesDBProtocolException("Response could not be handled", e);
        }
        for (HealingResult healingResult : healingResponse.getHealingResults()) {
            try {
                TiesServiceScopeResultAction.Result result = healingResult
                        .accept(new HealingResponseReader.HealingResult.Visitor<TiesServiceScopeResultAction.Result>() {
                            @Override
                            public TiesServiceScopeResultAction.Result on(HealingResultSuccess healingResultSuccess) {
                                return new TiesServiceScopeHealingAction.Result.Success() {
                                    @Override
                                    public byte[] getHeaderHash() {
                                        return healingResultSuccess.getEntryHeaderHash();
                                    }
                                };
                            }

                            @Override
                            public TiesServiceScopeResultAction.Result on(HealingResultError healingResultError) {
                                return new TiesServiceScopeHealingAction.Result.Error() {

                                    @Override
                                    public byte[] getHeaderHash() {
                                        return healingResultError.getEntryHeaderHash();
                                    }

                                    @Override
                                    public Throwable getError() {
                                        return new TiesException(healingResultError.getMessage());
                                    }
                                };
                            }
                        });
                serviceScope.result(new TiesServiceScopeResultAction() {

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
    public Void on(SchemaResponse schemaResponse) throws TiesDBProtocolException {
        throw new TiesDBProtocolException("Schema delegation is prohibited");
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

    private List<TiesServiceScopeRecollectionAction.Result.Field> convertFields(Collection<FieldReader.Field> fields) {
        List<TiesServiceScopeRecollectionAction.Result.Field> result = fields.parallelStream().map(field -> {
            byte[] raw = field.getRawValue();
            if (null != raw) {
                return new TiesServiceScopeRecollectionAction.Result.Field.RawField() {

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

                    @Override
                    public byte[] getValue() {
                        return getRawValue();
                    }

                    @Override
                    public byte[] getHash() {
                        return field.getHash();
                    }
                };
            } else {
                return new TiesServiceScopeRecollectionAction.Result.Field.HashField() {

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
