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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.util.FormatUtil;

import one.utopic.sparse.ebml.format.BytesFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

public class ModificationResultErrorReader implements Reader<ModificationResultErrorReader.ModificationResultError> {

    private static final Logger LOG = LoggerFactory.getLogger(ModificationResultErrorReader.class);

    public static class ModificationResultError implements ModificationResponseReader.ModificationResult {

        private byte[] entryHeaderHash;
        private String message;

        @Override
        public String toString() {
            return "ModificationResultError [entryHeaderHash=" + FormatUtil.printPartialHex(entryHeaderHash) + ", message=" + message + "]";
        }

        @Override
        public byte[] getEntryHeaderHash() {
            return entryHeaderHash;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

    }

    public boolean acceptResultSuccess(Conversation session, Event e, ModificationResultError r) throws TiesDBProtocolException {
        switch (e.getType()) {
        case ERROR_MESSAGE:
            r.message = session.read(UTF8StringFormat.INSTANCE);
            LOG.debug("ERROR_MESSAGE : {}", r.message);
            end(session, e);
            return true;
        case ENTRY_HASH:
            r.entryHeaderHash = session.read(BytesFormat.INSTANCE);
            LOG.debug("ENTRY_HASH : {}", r.entryHeaderHash);
            end(session, e);
            return true;
        // $CASES-OMITTED$
        default:
            return false;
        }
    }

    @Override
    public boolean accept(Conversation session, Event e, ModificationResultError r) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptResultSuccess, r);
        return true;
    }

}
