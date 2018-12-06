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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;

import one.utopic.sparse.ebml.format.ASCIIStringFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

public class SchemaFieldReader implements Reader<SchemaFieldReader.SchemaField> {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaFieldReader.class);

    public static class SchemaField {

        private String name;
        private String type;
        private final Boolean primary;

        public SchemaField(Boolean primary) {
            this.primary = primary;
        }

        @Override
        public String toString() {
            return "SchemaField [primary=" + primary + ", name=" + name + ", type=" + type + "]";
        }

        public Boolean getPrimary() {
            return primary;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

    }

    public boolean acceptField(Conversation session, Event e, SchemaField field) throws TiesDBProtocolException {
        switch (e.getType()) {
        case FIELD_NAME:
            field.name = session.read(UTF8StringFormat.INSTANCE);
            LOG.debug("FIELD_NAME: {}", field.name);
            end(session, e);
            return true;
        case FIELD_TYPE:
            field.type = session.read(ASCIIStringFormat.INSTANCE);
            LOG.debug("FIELD_TYPE: {}", field.type);
            end(session, e);
            return true;
        // $CASES-OMITTED$
        default:
            return false;
        }
    }

    @Override
    public boolean accept(Conversation session, Event e, SchemaField field) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptField, field);
        return true;
    }

}
