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

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.FIELD_NAME;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.FIELD_TYPE;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.write;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;

import one.utopic.sparse.ebml.format.ASCIIStringFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

public class FieldMetaWriter implements Writer<FieldMetaWriter.FieldMeta> {

    private static final Logger LOG = LoggerFactory.getLogger(FieldMetaWriter.class);

    public static interface FieldMeta {

        String getName();

        String getType();

    }

    @Override
    public void accept(Conversation session, FieldMeta fieldMeta) throws TiesDBProtocolException {
        LOG.trace("{}", fieldMeta);
        write( //
                write(FIELD_TYPE, ASCIIStringFormat.INSTANCE, fieldMeta.getType()), //
                write(FIELD_NAME, UTF8StringFormat.INSTANCE, fieldMeta.getName()) //
        ).accept(session);
    }

}
