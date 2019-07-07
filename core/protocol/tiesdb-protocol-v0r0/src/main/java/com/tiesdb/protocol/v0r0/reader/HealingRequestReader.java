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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.reader.ComputeRetrieveReader.ComputeRetrieve;
import com.tiesdb.protocol.v0r0.reader.FieldRetrieveReader.FieldRetrieve;

import one.utopic.sparse.ebml.format.BigIntegerFormat;

public class HealingRequestReader implements Reader<HealingRequestReader.HealingRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(HealingRequestReader.class);

    public static class HealingRequest implements Reader.Request {

        private BigInteger messageId;

        @Override
        public String toString() {
            return "HealingRequest [messageId=" + messageId + "]";
        }

        @Override
        public <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

        @Override
        public BigInteger getMessageId() {
            return messageId;
        }

    }

    public static interface Retrieve {

        interface Visitor<T> {

            T on(FieldRetrieve retrieve);

            T on(ComputeRetrieve retrieve);

        }

        <T> T accept(Visitor<T> v);

    }

    public boolean acceptHealingRequest(Conversation session, Event e, HealingRequest r) throws TiesDBProtocolException {
        switch (e.getType()) {
        case MESSAGE_ID:
            r.messageId = session.read(BigIntegerFormat.INSTANCE);
            LOG.debug("MESSAGE_ID : {}", r.messageId);
            end(session, e);
            return true;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return false;
    }
    @Override
    public boolean accept(Conversation session, Event e, HealingRequest request) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptHealingRequest, request);
        return true;
    }

}
