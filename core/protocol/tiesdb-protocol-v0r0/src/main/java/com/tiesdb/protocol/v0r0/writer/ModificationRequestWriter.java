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
import com.tiesdb.protocol.v0r0.ebml.TiesDBRequestConsistency;
import com.tiesdb.protocol.v0r0.ebml.format.TiesDBRequestConsistencyFormat;
import com.tiesdb.protocol.v0r0.writer.ModificationEntryWriter.ModificationEntry;

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.*;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.*;

import java.math.BigInteger;

import one.utopic.sparse.ebml.format.BigIntegerFormat;

public class ModificationRequestWriter implements Writer<ModificationRequestWriter.ModificationRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(ModificationRequestWriter.class);

    public static interface ModificationRequest extends Writer.Request {

        @Override
        public default <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

        public TiesDBRequestConsistency getConsistency();

        public Iterable<ModificationEntry> getEntries();

        @Override
        public BigInteger getMessageId();

    }

    private final ModificationEntryWriter modificationEntryWriter = new ModificationEntryWriter();

    @Override
    public void accept(Conversation session, ModificationRequest modificationRequest) throws TiesDBProtocolException {
        LOG.debug("ModificationRequest {}", modificationRequest);
        write(MODIFICATION_REQUEST, //
                write(CONSISTENCY, TiesDBRequestConsistencyFormat.INSTANCE, modificationRequest.getConsistency()), //
                write(MESSAGE_ID, BigIntegerFormat.INSTANCE, modificationRequest.getMessageId()), //
                write(modificationEntryWriter, modificationRequest.getEntries())//
        ).accept(session);
    }

}
