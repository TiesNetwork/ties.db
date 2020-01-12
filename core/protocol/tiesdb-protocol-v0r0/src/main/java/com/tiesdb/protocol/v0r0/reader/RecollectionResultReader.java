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
import static com.tiesdb.protocol.v0r0.reader.ReaderUtil.checkEntryFieldsHash;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.reader.EntryReader.Entry;
import com.tiesdb.protocol.v0r0.reader.FieldReader.Field;

public class RecollectionResultReader implements Reader<RecollectionResultReader.RecollectionEntry> {

    public static class RecollectionEntry extends Entry implements RecollectionResponseReader.RecollectionResult {

        private List<Field> computeFields = new LinkedList<>();

        public List<Field> getComputeFields() {
            return computeFields;
        }

        @Override
        public <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

    }

    private final EntryReader entryReader = new EntryReader();
    private final FieldListReader fieldListReader = new FieldListReader();

    private boolean acceptRecollectionResult(Conversation session, Event e, RecollectionEntry r) throws TiesDBProtocolException {
        switch (e.getType()) {
        case ENTRY:
            entryReader.accept(session, e, r);
            if (!checkEntryFieldsHash(r)) {
                throw new TiesDBProtocolException("Entry fields hash missmatch.");
            }
            return true;
        case RECOLLECTION_COMPUTE:
            fieldListReader.accept(session, e, r.computeFields::add);
            return true;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return false;
    }

    @Override
    public boolean accept(Conversation session, Event e, RecollectionEntry r) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptRecollectionResult, r);
        r.computeFields = Collections.unmodifiableList(r.computeFields);
        return true;
    }

}
