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
import com.tiesdb.protocol.v0r0.reader.EntryReader.Entry;

import one.utopic.sparse.ebml.format.BigIntegerFormat;

public class HealingRequestReader implements Reader<HealingRequestReader.HealingRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(HealingRequestReader.class);

    public static class HealingRequest implements Reader.Request {

        private BigInteger messageId;

        private List<Entry> healingEntries = new LinkedList<>();

        @Override
        public String toString() {
            return "HealingRequest [messageId=" + messageId + ", healingEntries="
                    + healingEntries + "]";
        }

        @Override
        public <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

        public List<Entry> getEntries() {
            return healingEntries;
        }

        @Override
        public BigInteger getMessageId() {
            return messageId;
        }

    }

    private final EntryReader healingEntryReader = new EntryReader();

    public boolean acceptHealingRequest(Conversation session, Event e, HealingRequest r) throws TiesDBProtocolException {
        switch (e.getType()) {
        case MESSAGE_ID:
            r.messageId = session.read(BigIntegerFormat.INSTANCE);
            LOG.debug("MESSAGE_ID : {}", r.messageId);
            end(session, e);
            return true;
        case ENTRY:
            Entry healingEntry = new Entry();
            boolean result = healingEntryReader.accept(session, e, healingEntry);
            if (result) {
                r.healingEntries.add(healingEntry);
            }
            return result;
        // $CASES-OMITTED$
        default:
            return false;
        }
    }

    @Override
    public boolean accept(Conversation session, Event e, HealingRequest r) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptHealingRequest, r);
        r.healingEntries = Collections.unmodifiableList(r.healingEntries);
        return true;
    }

}
