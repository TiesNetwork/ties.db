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
package com.tiesdb.protocol.v0r0.reader;

import static com.tiesdb.protocol.v0r0.reader.ReaderUtil.acceptEach;
import static com.tiesdb.protocol.v0r0.reader.ReaderUtil.end;

import java.math.BigInteger;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.reader.SchemaFieldReader.SchemaField;

import one.utopic.sparse.ebml.format.BigIntegerFormat;

public class SchemaResponseReader implements Reader<SchemaResponseReader.SchemaResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaResponseReader.class);

    public static class SchemaResponse implements Reader.Response {

        private BigInteger messageId;
        private LinkedList<SchemaField> fields = new LinkedList<>();

        @Override
        public String toString() {
            return "SchemaResponse [messageId=" + messageId + ", fields=" + fields + "]";
        }

        public BigInteger getMessageId() {
            return messageId;
        }

        public LinkedList<SchemaField> getFields() {
            return fields;
        }

        @Override
        public <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

    }

    private final SchemaFieldReader fieldReader = new SchemaFieldReader();

    public boolean acceptSchemaRequest(Conversation session, Event e, SchemaResponse r) throws TiesDBProtocolException {
        switch (e.getType()) {
        case MESSAGE_ID:
            r.messageId = session.read(BigIntegerFormat.INSTANCE);
            LOG.debug("MESSAGE_ID : {}", r.messageId);
            end(session, e);
            return true;
        case SCHEMA_FIELD: {
            SchemaField field = new SchemaFieldReader.SchemaField(false);
            if (fieldReader.accept(session, e, field)) {
                r.fields.add(field);
                return true;
            }
            break;
        }
        case SCHEMA_KEY_FIELD: {
            SchemaField field = new SchemaFieldReader.SchemaField(true);
            if (fieldReader.accept(session, e, field)) {
                r.fields.add(field);
                return true;
            }
            break;
        }
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return false;
    }

    @Override
    public boolean accept(Conversation session, Event e, SchemaResponse schemaRequest) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptSchemaRequest, schemaRequest);
        return true;
    }

}
