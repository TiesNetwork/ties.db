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

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.FIELD;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.FIELD_HASH;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.FIELD_NAME;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.FIELD_TYPE;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.FIELD_VALUE;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.write;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.ebml.TiesDBType;
import com.tiesdb.protocol.v0r0.writer.FieldWriter.Field.HashField;
import com.tiesdb.protocol.v0r0.writer.FieldWriter.Field.ValueField;
import com.tiesdb.protocol.v0r0.writer.FieldWriter.Field.Visitor;
import com.tiesdb.protocol.v0r0.writer.WriterUtil.ConversationConsumer;
import com.tiesdb.protocol.v0r0.writer.WriterUtil.ConversationFunction;

import one.utopic.sparse.ebml.EBMLFormat;
import one.utopic.sparse.ebml.format.ASCIIStringFormat;
import one.utopic.sparse.ebml.format.BytesFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

public class FieldWriter implements Writer<FieldWriter.Field> {

    private static final Logger LOG = LoggerFactory.getLogger(FieldWriter.class);

    public static interface Field {

        interface ValueField extends Field {

            EBMLFormat<Object> getFormat();

            Object getValue();

            @Override
            default <T> T accept(Visitor<T> v) {
                return v.on(this);
            }

        }

        interface HashField extends Field {

            byte[] getHash();

            @Override
            default <T> T accept(Visitor<T> v) {
                return v.on(this);
            }

        }

        interface Visitor<T> {

            T on(ValueField field);

            T on(HashField field);

        }

        <T> T accept(Visitor<T> v);

        String getName();

        String getType();

    }

    private static interface SpecificFieldWriter extends Visitor<ConversationConsumer>, ConversationFunction<Field> {
        @Override
        default ConversationConsumer accept(Field f) throws TiesDBProtocolException {
            return f.accept(this);
        }
    }

    private final SpecificFieldWriter specificFieldWriter = new SpecificFieldWriter() {

        @Override
        public ConversationConsumer on(HashField field) {
            return write(FIELD_HASH, BytesFormat.INSTANCE, field.getHash());

        }

        @Override
        public ConversationConsumer on(ValueField field) {
            return write(FIELD_VALUE, field.getFormat(), field.getValue());
        }

    };

    private final TiesDBType fieldType;

    public FieldWriter(TiesDBType fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public void accept(Conversation session, Field field) throws TiesDBProtocolException {
        LOG.debug("Field {}", field);
        write(fieldType, //
                write(FIELD_NAME, UTF8StringFormat.INSTANCE, field.getName()), //
                write(FIELD_TYPE, ASCIIStringFormat.INSTANCE, field.getType()), //
                write(specificFieldWriter, field) //
        ).accept(session);
    }

}
