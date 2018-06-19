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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.writer.ModificationResponseWriter.ModificationResult;

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.*;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.*;

import one.utopic.sparse.ebml.format.BytesFormat;

public class ModificationSuccessWriter implements Writer<ModificationSuccessWriter.ModificationSuccessResult> {

    private static final Logger LOG = LoggerFactory.getLogger(ModificationSuccessWriter.class);

    public static interface ModificationSuccessResult extends ModificationResult {

        byte[] getEntryHeaderHash();

        @Override
        default void accept(Visitor v) throws TiesDBProtocolException {
            v.on(this);
        }

    }

    @Override
    public void accept(Conversation session, ModificationSuccessResult result) throws TiesDBProtocolException {
        LOG.debug("ModificationSuccessResult {}", result);

        write(MODIFICATION_RESULT, //
                write(ENTRY_HASH, BytesFormat.INSTANCE, result.getEntryHeaderHash()) //
        ).accept(session);

    }

}
