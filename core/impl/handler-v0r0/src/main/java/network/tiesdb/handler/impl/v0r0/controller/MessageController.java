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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBException;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.EventState;
import com.tiesdb.protocol.v0r0.reader.EntryHeaderReader.EntryHeader;
import com.tiesdb.protocol.v0r0.reader.FieldReader;
import com.tiesdb.protocol.v0r0.reader.MessageReader;
import com.tiesdb.protocol.v0r0.reader.EntryReader;
import com.tiesdb.protocol.v0r0.reader.Reader;
import com.tiesdb.protocol.v0r0.reader.Reader.Request;
import com.tiesdb.protocol.v0r0.writer.ResponseWriter;

import network.tiesdb.service.api.TiesService;
import network.tiesdb.service.scope.api.TiesCheque;
import network.tiesdb.service.scope.api.TiesEntryExtended;
import network.tiesdb.service.scope.api.TiesEntryHeader;
import network.tiesdb.service.scope.api.TiesServiceScopeException;

public class MessageController {

    private static final Logger LOG = LoggerFactory.getLogger(MessageController.class);

    protected static class EntryImpl implements TiesEntryExtended {

        private final TiesEntryHeader header;
        private final String tablespaceName;
        private final String tableName;
        private final Map<String, TypedValueField> fieldValues;
        private final Map<String, TypedHashField> fieldHashes;
        private final List<? extends TiesCheque> cheques;

        public EntryImpl(EntryReader.Entry modificationEntry, boolean forInsert) throws TiesServiceScopeException {
            this.tablespaceName = modificationEntry.getHeader().getTablespaceName();
            this.tableName = modificationEntry.getHeader().getTableName();
            Map<String, TypedValueField> fieldValues = new HashMap<>();
            Map<String, TypedHashField> fieldHashes = new HashMap<>();
            for (Map.Entry<String, FieldReader.Field> e : modificationEntry.getFields().entrySet()) {
                FieldReader.Field field = e.getValue();
                if (null != field.getRawValue()) {
                    Object fieldValue = deserialize(field);
                    fieldValues.put(e.getKey(), new TypedValueField() {

                        @Override
                        public String getType() {
                            return field.getType();
                        }

                        @Override
                        public byte[] getHash() {
                            return field.getHash();
                        }

                        @Override
                        public byte[] getValue() {
                            return field.getRawValue();
                        }

                        @Override
                        public String getName() {
                            return field.getName();
                        }

                        @Override
                        public Object getObject() {
                            return fieldValue;
                        }

                    });
                } else if (forInsert) {
                    throw new TiesServiceScopeException("Insert should have only value fields");
                } else {
                    fieldHashes.put(e.getKey(), new TypedHashField() {

                        @Override
                        public byte[] getHash() {
                            return field.getHash();
                        }

                        @Override
                        public String getName() {
                            return field.getName();
                        }

                        @Override
                        public String getType() {
                            return field.getType();
                        }

                    });
                }
            }
            this.fieldHashes = fieldHashes;
            this.fieldValues = fieldValues;
            this.header = new TiesEntryHeader() {

                private final EntryHeader header = modificationEntry.getHeader();

                @Override
                public Date getEntryTimestamp() {
                    return header.getEntryTimestamp();
                }

                @Override
                public short getEntryNetwork() {
                    return header.getEntryNetwork().shortValue();
                }

                @Override
                public BigInteger getEntryVersion() {
                    return header.getEntryVersion();
                }

                @Override
                public byte[] getEntryFldHash() {
                    return header.getEntryFldHash();
                }

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
                public byte[] getEntryOldHash() {
                    return header.getEntryOldHash();
                }

            };
            cheques = modificationEntry.getCheques().parallelStream().map(cheque -> new TiesCheque() {

                private final List<Address> addresses = cheque.getChequeAddresses().parallelStream().map(address -> new Address() {

                    @Override
                    public byte[] getAddress() {
                        return address.getAddress();
                    }

                }).collect(Collectors.toList());

                @Override
                public byte[] getSigner() {
                    return cheque.getSigner();
                }

                @Override
                public byte[] getSignature() {
                    return cheque.getSignature();
                }

                @Override
                public BigInteger getChequeVersion() {
                    return cheque.getChequeVersion();
                }

                @Override
                public BigInteger getChequeNetwork() {
                    return cheque.getChequeNetwork();
                }

                @Override
                public byte[] getHash() {
                    return cheque.getHash();
                }

                @Override
                public Date getChequeTimestamp() {
                    return cheque.getChequeTimestamp();
                }

                @Override
                public UUID getChequeRange() {
                    return cheque.getChequeRange();
                }

                @Override
                public BigInteger getChequeNumber() {
                    return cheque.getChequeNumber();
                }

                @Override
                public BigInteger getChequeAmount() {
                    return cheque.getChequeAmount();
                }

                @Override
                public List<Address> getChequeAddresses() {
                    return addresses;
                }

            }).collect(Collectors.toList());
        }

        @Override
        public String getTablespaceName() {
            return tablespaceName;
        }

        @Override
        public String getTableName() {
            return tableName;
        }

        @Override
        public Map<String, TypedHashField> getFieldHashes() {
            return fieldHashes;
        }

        @Override
        public Map<String, TypedValueField> getFieldValues() {
            return fieldValues;
        }

        @Override
        public TiesEntryHeader getHeader() {
            return header;
        }

        @Override
        public List<? extends TiesCheque> getCheques() {
            return cheques;
        }
    }

    static Object deserialize(FieldReader.Field field) throws TiesServiceScopeException {
        return ControllerUtil.readerForType(field.getType()).apply(field.getRawValue());
    }

    private final MessageReader messageReader;
    private final RequestHandler requestController;
    private final ResponseHandler responseController;

    public MessageController(TiesService service) {
        this.messageReader = new MessageReader();
        this.requestController = new RequestHandler(service, new ResponseWriter());
        this.responseController = new ResponseHandler(service);
    }

    public void handle(Conversation session) throws TiesDBException {
        Event event;
        while (null != messageReader && null != (event = session.get())) {
            LOG.debug("RootBeginEvent: {}", event);
            if (EventState.BEGIN.equals(event.getState())) {
                if (messageReader.accept(session, event, message -> {
                    message.accept(new Reader.Message.Visitor() {

                        @Override
                        public void on(Request request) throws TiesDBProtocolException {
                            requestController.handle(session, request);
                        }

                        @Override
                        public void on(com.tiesdb.protocol.v0r0.reader.Reader.Response response) throws TiesDBProtocolException {
                            responseController.handle(session, response);
                        }
                    });
                })) {
                    continue;
                }
                LOG.warn("Skipped {}", event);
                session.skip();
                Event endEvent = session.get();
                LOG.debug("RootEndEvent: {}", endEvent);
                if (null != endEvent && EventState.END.equals(endEvent.getState()) && endEvent.getType().equals(event.getType())) {
                    continue;
                }
            }
            throw new TiesDBProtocolException("Illegal root event: " + event);
        }
    }

}
