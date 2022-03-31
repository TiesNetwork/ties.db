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
import static network.tiesdb.util.Hex.UPPERCASE_HEX;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bouncycastle.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.ebml.TiesDBRequestConsistency;
import com.tiesdb.protocol.v0r0.exception.TiesDBProtocolMessageException;
import com.tiesdb.protocol.v0r0.reader.BillingRequestReader.BillingRequest;
import com.tiesdb.protocol.v0r0.reader.EntryHeaderReader;
import com.tiesdb.protocol.v0r0.reader.EntryReader;
import com.tiesdb.protocol.v0r0.reader.HealingRequestReader.HealingRequest;
import com.tiesdb.protocol.v0r0.reader.ModificationRequestReader.ModificationRequest;
import com.tiesdb.protocol.v0r0.reader.Reader.Request;
import com.tiesdb.protocol.v0r0.reader.RecollectionRequestReader.RecollectionRequest;
import com.tiesdb.protocol.v0r0.reader.SchemaRequestReader.SchemaRequest;
import com.tiesdb.protocol.v0r0.writer.BillingResponseWriter;
import com.tiesdb.protocol.v0r0.writer.EntryHeaderWriter.EntryHeader;
import com.tiesdb.protocol.v0r0.writer.FieldWriter.Field;
import com.tiesdb.protocol.v0r0.writer.FieldWriter.Field.HashField;
import com.tiesdb.protocol.v0r0.writer.FieldWriter.Field.ValueField;
import com.tiesdb.protocol.v0r0.writer.HealingResponseWriter.HealingResponse;
import com.tiesdb.protocol.v0r0.writer.HealingResponseWriter.HealingResult;
import com.tiesdb.protocol.v0r0.writer.HealingResultErrorWriter.HealingResultError;
import com.tiesdb.protocol.v0r0.writer.HealingResultSuccessWriter.HealingResultSuccess;
import com.tiesdb.protocol.v0r0.writer.ModificationResponseWriter.ModificationResponse;
import com.tiesdb.protocol.v0r0.writer.ModificationResponseWriter.ModificationResult;
import com.tiesdb.protocol.v0r0.writer.ModificationResultErrorWriter.ModificationResultError;
import com.tiesdb.protocol.v0r0.writer.ModificationResultSuccessWriter.ModificationResultSuccess;
import com.tiesdb.protocol.v0r0.writer.Multiple;
import com.tiesdb.protocol.v0r0.writer.RecollectionErrorWriter.RecollectionError;
import com.tiesdb.protocol.v0r0.writer.RecollectionResponseWriter.RecollectionResponse;
import com.tiesdb.protocol.v0r0.writer.RecollectionResponseWriter.RecollectionResult;
import com.tiesdb.protocol.v0r0.writer.RecollectionResultWriter.RecollectionEntry;
import com.tiesdb.protocol.v0r0.writer.ResponseWriter;
import com.tiesdb.protocol.v0r0.writer.ChequeWriter.Cheque;
import com.tiesdb.protocol.v0r0.writer.SchemaFieldWriter.SchemaField;
import com.tiesdb.protocol.v0r0.writer.SchemaResponseWriter.SchemaResponse;
import com.tiesdb.protocol.v0r0.writer.Writer.Response;

import network.tiesdb.handler.impl.v0r0.controller.ControllerUtil.WriteConverter;
import network.tiesdb.service.api.TiesService;
import network.tiesdb.service.scope.api.TiesCheque;
import network.tiesdb.service.scope.api.TiesEntryExtended;
import network.tiesdb.service.scope.api.TiesEntryHeader;
import network.tiesdb.service.scope.api.TiesServiceScope;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.CountConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.PercentConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.QuorumConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeBillingAction;
import network.tiesdb.service.scope.api.TiesServiceScopeException;
import network.tiesdb.service.scope.api.TiesServiceScopeHealingAction;
import network.tiesdb.service.scope.api.TiesServiceScopeModificationAction;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollectionAction;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollectionAction.Result;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollectionAction.Result.Field.RawField;
import network.tiesdb.service.scope.api.TiesServiceScopeSchemaAction;
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

    public RecollectionEntry convertToRecollectionResultEntry(RecollectionRequest request, Result.Entry entry) {
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
        return new RecollectionEntry() {

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
                            return "HashField [name=" + getName() + ", type=" + getType() + ", hash" + printHexValue(getHash()) + "]";
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
                            return wc.convert(field.getFieldValue());
                        }

                        @Override
                        public String toString() {
                            return "ValueField [name=" + getName() + ", type=" + getType() + ", value" + printValue(getType(), getValue())
                                    + "]";
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
                            return "ValueField [name=" + getName() + ", type=" + getType() + ", value" + printValue(getType(), getValue())
                                    + "]";
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
                    return "HashField [name=" + getName() + ", type=" + getType() + ", hash" + printHexValue(getHash()) + "]";
                }

            };
        }
    }

    protected static String printValue(String type, Object value) {
        if (null == value) {
            return " is null";
        }
        if (value instanceof byte[]) {
            if (null != type) {
                switch (type) {
                case "uuid":
                    ByteBuffer uuidBuf = ByteBuffer.allocate(2 * Long.BYTES);
                    uuidBuf.put((byte[]) value).flip();
                    return "=" + new UUID(uuidBuf.getLong(), uuidBuf.getLong()).toString();
                default:
                    // NOP
                }
            }
            return printHexValue((byte[]) value);
        }
        return value.toString();
    }

    protected static String printHexValue(byte[] value) {
        if (null == value) {
            return " is null";
        }
        if (value.length <= 64) {
            return "=0x" + UPPERCASE_HEX.printHexBinary(value);
        } else {
            return "=0x" + UPPERCASE_HEX.printHexBinary(Arrays.copyOfRange(value, 0, 32)) + "..." //
                    + UPPERCASE_HEX.printHexBinary(Arrays.copyOfRange(value, value.length - 32, value.length)) //
                    + "(" + value.length + ")";
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

        TiesServiceScope serviceScope;
        try {
            serviceScope = service.newServiceScope();
        } catch (TiesServiceScopeException e) {
            LOG.error("Error handling ModificationRequest {}", request, e);
            throw new TiesDBProtocolMessageException(messageId, "Error handling ModificationRequest", e);
        }
        LinkedList<TiesServiceScopeModificationAction.Result> results = new LinkedList<>();
        for (EntryReader.Entry modificationEntry : request.getEntries()) {
            EntryHeaderReader.EntryHeader header = modificationEntry.getHeader();
            if (null == header) {
                IllegalArgumentException e = new IllegalArgumentException("No header");
                LOG.error("Error handling ModificationRequest.Entry {}", modificationEntry, e);
                results.add(new TiesServiceScopeModificationAction.Result.Error() {

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
                    serviceScope.insert(new TiesServiceScopeModificationAction() {

                        private final EntryImpl entry = new EntryImpl(modificationEntry, true);

                        @Override
                        public TiesEntryExtended getEntry() {
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
                    serviceScope.delete(new TiesServiceScopeModificationAction() {

                        private final EntryImpl entry = new EntryImpl(modificationEntry, false);

                        @Override
                        public TiesEntryExtended getEntry() {
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
                    serviceScope.update(new TiesServiceScopeModificationAction() {

                        private final EntryImpl entry = new EntryImpl(modificationEntry, false);

                        @Override
                        public TiesEntryExtended getEntry() {
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
                results.add(new TiesServiceScopeModificationAction.Result.Error() {

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
                return r.accept(new TiesServiceScopeModificationAction.Result.Visitor<ModificationResult>() {
                    @Override
                    public ModificationResult on(TiesServiceScopeModificationAction.Result.Success success)
                            throws TiesServiceScopeException {
                        return new ModificationResultSuccess() {
                            @Override
                            public byte[] getEntryHeaderHash() {
                                return success.getHeaderHash();
                            }
                        };
                    }

                    @Override
                    public ModificationResult on(TiesServiceScopeModificationAction.Result.Error error) throws TiesServiceScopeException {
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

        TiesServiceScope serviceScope;
        try {
            serviceScope = service.newServiceScope();
        } catch (TiesServiceScopeException e) {
            LOG.error("Error handling RecollectionRequest {}", request, e);
            throw new TiesDBProtocolMessageException(messageId, "Error handling RecollectionRequest", e);
        }
        List<RecollectionResult> results = new LinkedList<>();
        try {
            serviceScope.select(new TiesServiceScopeRecollectionAction() {

                private final Query query = new QueryImpl(request);

                @Override
                public Query getQuery() {
                    return this.query;
                }

                @Override
                public void setResult(Result r) throws TiesServiceScopeException {
                    LOG.debug("AddedResult {}", r);
                    r.accept(new TiesServiceScopeRecollectionAction.Result.Visitor<Stream<RecollectionResult>>() {

                        @Override
                        public Stream<RecollectionResult> on(Success success) throws TiesServiceScopeException {
                            return success.getEntries().stream().map(entry -> convertToRecollectionResultEntry(request, entry));
                        }

                        @Override
                        public Stream<RecollectionResult> on(Error error) throws TiesServiceScopeException {
                            return error.getErrors().stream().map(th -> new RecollectionError() {
                                @Override
                                public Throwable getError() {
                                    return th;
                                }

                                @Override
                                public String toString() {
                                    return "RecollectionError [" + getError() + "]";
                                }
                            });
                        }

                        @Override
                        public Stream<RecollectionResult> on(Partial partial) throws TiesServiceScopeException {
                            if (partial.isSuccess() && !partial.isError()) {
                                return on((Success) partial);
                            } else if (!partial.isSuccess() && partial.isError()) {
                                return on((Error) partial);
                            }
                            return Stream.concat(on((Success) partial), on((Error) partial));
                        }
                    }).forEach(results::add);

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
        }

    }

    @Override
    public Response on(HealingRequest request) throws TiesDBProtocolException {

        requireNonNull(request);

        BigInteger messageId = request.getMessageId();
        LOG.debug("MessageID: {}", messageId);

        TiesServiceScope serviceScope;
        try {
            serviceScope = service.newServiceScope();
        } catch (TiesServiceScopeException e) {
            LOG.error("Error handling HealingRequest {}", request, e);
            throw new TiesDBProtocolMessageException(messageId, "Error handling HealingRequest", e);
        }
        LinkedList<TiesServiceScopeHealingAction.Result> results = new LinkedList<>();
        for (EntryReader.Entry healingEntry : request.getEntries()) {
            EntryHeaderReader.EntryHeader header = healingEntry.getHeader();
            if (null == header) {
                IllegalArgumentException e = new IllegalArgumentException("No header");
                LOG.error("Error handling HealingRequest.Entry {}", healingEntry, e);
                results.add(new TiesServiceScopeHealingAction.Result.Error() {

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
                serviceScope.heal(new TiesServiceScopeHealingAction() {

                    private final EntryImpl entry = new EntryImpl(healingEntry, false);

                    @Override
                    public TiesEntryExtended getEntry() {
                        return entry;
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
            } catch (TiesServiceScopeException e) {
                LOG.error("Error handling HealingRequest.Entry {}", healingEntry, e);
                results.add(new TiesServiceScopeHealingAction.Result.Error() {

                    @Override
                    public Throwable getError() {
                        return e;
                    }

                    @Override
                    public byte[] getHeaderHash() {
                        return healingEntry.getHeader().getHash();
                    }
                });
                continue;
            }
        }
        final List<HealingResult> resultList = Collections.unmodifiableList(results.stream().map(r -> {
            try {
                return r.accept(new TiesServiceScopeHealingAction.Result.Visitor<HealingResult>() {
                    @Override
                    public HealingResult on(TiesServiceScopeHealingAction.Result.Success success) throws TiesServiceScopeException {
                        return new HealingResultSuccess() {
                            @Override
                            public byte[] getEntryHeaderHash() {
                                return success.getHeaderHash();
                            }
                        };
                    }

                    @Override
                    public HealingResult on(TiesServiceScopeHealingAction.Result.Error error) throws TiesServiceScopeException {
                        return new HealingResultError() {
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
                return new HealingResultError() {
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
        return new HealingResponse() {

            @Override
            public BigInteger getMessageId() {
                return messageId;
            }

            @Override
            public Iterable<HealingResult> getResults() {
                return resultList;
            }

        };
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

        LinkedList<SchemaField> schemaFields = new LinkedList<>();
        try {
            TiesServiceScope serviceScope = service.newServiceScope();
            serviceScope.schema(new TiesServiceScopeSchemaAction() {

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

    @Override
    public Response on(BillingRequest request) throws TiesDBProtocolException {
        requireNonNull(request);

        BigInteger messageId = request.getMessageId();
        LOG.debug("MessageID: {}", messageId);

        LinkedList<Cheque> resultCheques = new LinkedList<>();

        try {
            TiesServiceScope serviceScope = service.newServiceScope();
            serviceScope.billing(new TiesServiceScopeBillingAction() {

                @Override
                public BigInteger getMessageId() {
                    return messageId;
                }

                @Override
                public BigInteger getChequesCountLimit() {
                    return request.getCountLimit();
                }

                @Override
                public BigInteger getChequesCropAmountThreshold() {
                    return request.getAmountThreshold();
                }

                @Override
                public void setResult(List<TiesCheque> cheques) throws TiesServiceScopeException {
                    cheques.stream()//
                            .map(cheque -> new Cheque() {

                                @Override
                                public byte[] getSigner() {
                                    return cheque.getSigner();
                                }

                                @Override
                                public byte[] getSignature() {
                                    return cheque.getSignature();
                                }

                                @Override
                                public String getTablespaceName() {
                                    return cheque.getTablespaceName();
                                }

                                @Override
                                public String getTableName() {
                                    return cheque.getTableName();
                                }

                                @Override
                                public BigInteger getChequeVersion() {
                                    return cheque.getChequeVersion();
                                }

                                @Override
                                public UUID getChequeSession() {
                                    return cheque.getChequeSession();
                                }

                                @Override
                                public BigInteger getChequeNumber() {
                                    return cheque.getChequeNumber();
                                }

                                @Override
                                public BigInteger getChequeNetwork() {
                                    return cheque.getChequeNetwork();
                                }

                                @Override
                                public BigInteger getChequeCropAmount() {
                                    return cheque.getChequeCropAmount();
                                }
                            })//
                            .sequential().forEach(resultCheques::add);
                }

            });
        } catch (TiesServiceScopeException e) {
            LOG.error("Error handling SchemaRequest {}", request, e);
            throw new TiesDBProtocolMessageException(messageId, "Error handling SchemaRequest", e);
        }

        return new BillingResponseWriter.BillingResponse() {

            @Override
            public BigInteger getMessageId() {
                return messageId;
            }

            @Override
            public Iterable<Cheque> getCheques() {
                return resultCheques;
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
