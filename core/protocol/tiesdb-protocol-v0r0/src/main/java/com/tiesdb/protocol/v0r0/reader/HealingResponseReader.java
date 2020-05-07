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
import com.tiesdb.protocol.v0r0.reader.HealingResultErrorReader.HealingResultError;
import com.tiesdb.protocol.v0r0.reader.HealingResultSuccessReader.HealingResultSuccess;

import one.utopic.sparse.ebml.format.BigIntegerFormat;

public class HealingResponseReader implements Reader<HealingResponseReader.HealingResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(HealingResponseReader.class);

    public static class HealingResponse implements Reader.Response {

        private BigInteger messageId;
        private List<HealingResult> healingResults = new LinkedList<>();

        @Override
        public String toString() {
            return "HealingResponse [messageId=" + messageId + "]";
        }

        @Override
        public <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

        @Override
        public BigInteger getMessageId() {
            return messageId;
        }

        public List<HealingResult> getHealingResults() {
            return healingResults;
        }

    }

    public static interface HealingResult {

        interface Visitor<T> {

            T on(HealingResultSuccess modificationResultSuccess);

            T on(HealingResultError modificationResultError);

        }

        <T> T accept(Visitor<T> v) throws TiesDBProtocolException;

        byte[] getEntryHeaderHash();

    }

    private final HealingResultSuccessReader healingResultSuccessReader = new HealingResultSuccessReader();
    private final HealingResultErrorReader healingResultErrorReader = new HealingResultErrorReader();

    public boolean acceptHealingResponse(Conversation session, Event e, HealingResponse r) throws TiesDBProtocolException {
        switch (e.getType()) {
        case MESSAGE_ID:
            r.messageId = session.read(BigIntegerFormat.INSTANCE);
            LOG.debug("MESSAGE_ID : {}", r.messageId);
            end(session, e);
            return true;
        case HEALING_RESULT: {
            LOG.debug("HEALING_RESULT found in message {}", r.getMessageId());
            HealingResultSuccess healingResultSuccess = new HealingResultSuccess();
            boolean result = healingResultSuccessReader.accept(session, e, healingResultSuccess);
            if (result) {
                r.healingResults.add(healingResultSuccess);
            }
            return result;
        }
        case HEALING_ERROR: {
            LOG.debug("HEALING_ERROR found in message {}", r.getMessageId());
            HealingResultError healingResultError = new HealingResultError();
            boolean result = healingResultErrorReader.accept(session, e, healingResultError);
            if (result) {
                r.healingResults.add(healingResultError);
            }
            return result;
        }
        // $CASES-OMITTED$
        default:
            return false;
        }
    }

    @Override
    public boolean accept(Conversation session, Event e, HealingResponse r) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptHealingResponse, r);
        r.healingResults = Collections.unmodifiableList(r.healingResults);
        return true;
    }

}
