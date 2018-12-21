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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBException;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.EventState;
import com.tiesdb.protocol.v0r0.reader.EntryHeaderReader.EntryHeader;
import com.tiesdb.protocol.v0r0.reader.FieldReader.Field;
import com.tiesdb.protocol.v0r0.reader.MessageReader;
import com.tiesdb.protocol.v0r0.reader.ModificationEntryReader.ModificationEntry;
import com.tiesdb.protocol.v0r0.reader.Reader;
import com.tiesdb.protocol.v0r0.reader.Reader.Request;
import com.tiesdb.protocol.v0r0.writer.ResponseWriter;

import network.tiesdb.service.api.TiesService;
import network.tiesdb.service.scope.api.TiesEntryHeader;
import network.tiesdb.service.scope.api.TiesServiceScopeException;
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Entry;

public class MessageController {

    private static final Logger LOG = LoggerFactory.getLogger(MessageController.class);

    protected static class EntryImpl implements Entry {

        private final ModificationEntry modificationEntry;
        private final Map<String, Entry.FieldValue> fieldValues;
        private final Map<String, Entry.FieldHash> fieldHashes;

        public EntryImpl(ModificationEntry modificationEntry, boolean forInsert) throws TiesServiceScopeException {
            this.modificationEntry = modificationEntry;
            Map<String, Entry.FieldValue> fieldValues = new HashMap<>();
            Map<String, Entry.FieldHash> fieldHashes = new HashMap<>();
            for (Map.Entry<String, Field> e : modificationEntry.getFields().entrySet()) {
                Field field = e.getValue();
                if (null != field.getRawValue()) {
                    Object fieldValue = deserialize(field);
                    fieldValues.put(e.getKey(), new Entry.FieldValue() {

                        @Override
                        public String getType() {
                            return field.getType();
                        }

                        @Override
                        public byte[] getHash() {
                            return field.getHash();
                        }

                        @Override
                        public byte[] getBytes() {
                            return field.getRawValue();
                        }

                        @Override
                        public Object get() {
                            return fieldValue;
                        }
                    });
                } else if (forInsert) {
                    throw new TiesServiceScopeException("Insert should have only value fields");
                } else {
                    fieldHashes.put(e.getKey(), new Entry.FieldHash() {
                        @Override
                        public byte[] getHash() {
                            return field.getHash();
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
        }

        @Override
        public String getTablespaceName() {
            return modificationEntry.getHeader().getTablespaceName();
        }

        @Override
        public String getTableName() {
            return modificationEntry.getHeader().getTableName();
        }

        @Override
        public Map<String, Entry.FieldHash> getFieldHashes() {
            return fieldHashes;
        }

        @Override
        public Map<String, Entry.FieldValue> getFieldValues() {
            return fieldValues;
        }

        @Override
        public TiesEntryHeader getHeader() {
            EntryHeader header = modificationEntry.getHeader();
            return new TiesEntryHeader() {

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
        }
    }

    static Object deserialize(Field field) throws TiesServiceScopeException {
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
