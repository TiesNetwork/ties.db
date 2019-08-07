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
package com.tiesdb.protocol.v0r0.writer;

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.MESSAGE_ID;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.SCHEMA_RESPONSE;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.SCHEMA_FIELD;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.SCHEMA_KEY_FIELD;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.write;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.writer.SchemaFieldWriter.SchemaField;

import one.utopic.sparse.ebml.format.BigIntegerFormat;

public class SchemaResponseWriter implements Writer<SchemaResponseWriter.SchemaResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaResponseWriter.class);

    public static interface SchemaResponse extends Writer.Response {

        @Override
        public default <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

        public Iterable<SchemaField> getFields();

    }

    SchemaFieldWriter schemaFieldWriter = new SchemaFieldWriter(SCHEMA_FIELD);
    SchemaFieldWriter schemaKeyFieldWriter = new SchemaFieldWriter(SCHEMA_KEY_FIELD);

    public void acceptField(Conversation session, SchemaField field) throws TiesDBProtocolException {
        (field.isPrimary() ? schemaKeyFieldWriter : schemaFieldWriter).accept(session, field);
    }

    @Override
    public void accept(Conversation session, SchemaResponse response) throws TiesDBProtocolException {
        LOG.debug("SchemaResponse {}", response);
        write(SCHEMA_RESPONSE, //
                write(MESSAGE_ID, BigIntegerFormat.INSTANCE, response.getMessageId()), //
                write(this::acceptField, response.getFields()) //
        ).accept(session);

    }

}
