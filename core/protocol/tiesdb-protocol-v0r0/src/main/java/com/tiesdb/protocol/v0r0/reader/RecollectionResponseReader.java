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
import com.tiesdb.protocol.v0r0.reader.RecollectionResultReader.RecollectionResult;

import one.utopic.sparse.ebml.format.BigIntegerFormat;

public class RecollectionResponseReader implements Reader<RecollectionResponseReader.RecollectionResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(RecollectionResponseReader.class);

    public static class RecollectionResponse implements Reader.Response {

        private BigInteger messageId;

        private List<RecollectionResult> recollectionResults = new LinkedList<>();

        @Override
        public String toString() {
            return "RecollectionResponse [messageId=" + messageId + ", recollectionResults=" + recollectionResults + "]";
        }

        @Override
        public <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

        public List<RecollectionResult> getRecollectionResults() {
            return recollectionResults;
        }

        @Override
        public BigInteger getMessageId() {
            return messageId;
        }

    }

    private final RecollectionResultReader recollectionResultReader = new RecollectionResultReader();

    public boolean acceptRecollectionResponse(Conversation session, Event e, RecollectionResponse r) throws TiesDBProtocolException {
        switch (e.getType()) {
        case MESSAGE_ID:
            r.messageId = session.read(BigIntegerFormat.INSTANCE);
            LOG.debug("MESSAGE_ID : {}", r.messageId);
            end(session, e);
            return true;
        case RECOLLECTION_RESULT:
            LOG.debug("RECOLLECTION_RESULT found in message {}", r.getMessageId());
            RecollectionResult recollectionResult = new RecollectionResult();
            boolean result = recollectionResultReader.accept(session, e, recollectionResult);
            if (result) {
                r.recollectionResults.add(recollectionResult);
            }
            return result;
        // $CASES-OMITTED$
        default:
            return false;
        }
    }

    @Override
    public boolean accept(Conversation session, Event e, RecollectionResponse r) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptRecollectionResponse, r);
        r.recollectionResults = Collections.unmodifiableList(r.recollectionResults);
        return true;
    }

}
