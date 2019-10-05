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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.reader.ModificationResultErrorReader.ModificationResultError;
import com.tiesdb.protocol.v0r0.reader.ModificationResultSuccessReader.ModificationResultSuccess;

import one.utopic.sparse.ebml.format.BigIntegerFormat;

public class ModificationResponseReader implements Reader<ModificationResponseReader.ModificationResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(ModificationResponseReader.class);

    public static class ModificationResponse implements Reader.Response {

        private BigInteger messageId;

        private List<ModificationResult> modificationResults = new LinkedList<>();

        @Override
        public String toString() {
            return "ModificationResponse [messageId=" + messageId + ", modificationResults=" + modificationResults + "]";
        }

        @Override
        public <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

        public List<ModificationResult> getModificationResults() {
            return modificationResults;
        }

        @Override
        public BigInteger getMessageId() {
            return messageId;
        }

    }

    public static interface ModificationResult {

        interface Visitor<T> {

            T on(ModificationResultSuccess modificationResultSuccess);

            T on(ModificationResultError modificationResultError);

        }

        <T> T accept(Visitor<T> v) throws TiesDBProtocolException;

        byte[] getEntryHeaderHash();

    }

    private final ModificationResultSuccessReader modificationResultSuccessReader = new ModificationResultSuccessReader();
    private final ModificationResultErrorReader modificationResultErrorReader = new ModificationResultErrorReader();

    public boolean acceptModificationResult(Conversation session, Event e, ModificationResponse r) throws TiesDBProtocolException {
        switch (e.getType()) {
        case MESSAGE_ID: {
            r.messageId = session.read(BigIntegerFormat.INSTANCE);
            LOG.debug("MESSAGE_ID : {}", r.messageId);
            end(session, e);
            return true;
        }
        case MODIFICATION_RESULT: {
            LOG.debug("MODIFICATION_RESULT found in message {}", r.getMessageId());
            ModificationResultSuccess modificationResultSuccess = new ModificationResultSuccess();
            boolean result = modificationResultSuccessReader.accept(session, e, modificationResultSuccess);
            if (result) {
                r.modificationResults.add(modificationResultSuccess);
            }
            return result;
        }
        case MODIFICATION_ERROR: {
            LOG.debug("MODIFICATION_ERROR found in message {}", r.getMessageId());
            ModificationResultError modificationResultError = new ModificationResultError();
            boolean result = modificationResultErrorReader.accept(session, e, modificationResultError);
            if (result) {
                r.modificationResults.add(modificationResultError);
            }
            return result;
        }
        // $CASES-OMITTED$
        default:
            return false;
        }
    }

    @Override
    public boolean accept(Conversation session, Event e, ModificationResponse r) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptModificationResult, r);
        r.modificationResults = Collections.unmodifiableList(r.modificationResults);
        return true;
    }

}
