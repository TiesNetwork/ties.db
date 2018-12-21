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
import java.util.HashMap;
import java.util.LinkedList;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.reader.EntryHeaderReader.EntryHeader;
import com.tiesdb.protocol.v0r0.reader.FieldReader.Field;
import com.tiesdb.protocol.v0r0.util.CheckedConsumer;

public class RecollectionResultReader implements Reader<RecollectionResultReader.RecollectionResult> {

    public static class RecollectionResult implements Entry {

        private EntryHeader header;
        private HashMap<String, Field> fields = new HashMap<>();
        private LinkedList<Field> computeFields = new LinkedList<>();

        public EntryHeader getHeader() {
            return header;
        }

        public HashMap<String, Field> getFields() {
            return fields;
        }

        public LinkedList<Field> getComputeFields() {
            return computeFields;
        }

    }

    private final EntryHeaderReader entryHeaderReader = new EntryHeaderReader();
    private final FieldReader fieldReader = new FieldReader();

    public boolean acceptFieldList(Conversation session, Event e, CheckedConsumer<Field, TiesDBProtocolException> c)
            throws TiesDBProtocolException {
        switch (e.getType()) {
        case FIELD:
            Field f = new Field();
            if (fieldReader.accept(session, e, f)) {
                c.accept(f);
                return true;
            }
            break;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return false;
    }

    private boolean acceptEntry(Conversation session, Event e, RecollectionResult r) throws TiesDBProtocolException {
        switch (e.getType()) {
        case ENTRY_HEADER:
            EntryHeader header = new EntryHeader();
            boolean result = entryHeaderReader.accept(session, e, header);
            if (result) {
                if (null != r.header) {
                    throw new TiesDBProtocolException("Multiple headers detected! Should be only one header in each entry.");
                }
                r.header = header;
            }
            return true;
        case FIELD_LIST:
            acceptEach(session, e, this::acceptFieldList, f -> r.fields.put(f.getName(), f));
            return true;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return false;
    }

    private boolean acceptRecollectionResult(Conversation session, Event e, RecollectionResult r) throws TiesDBProtocolException {
        switch (e.getType()) {
        case ENTRY:
            acceptEach(session, e, this::acceptEntry, r);
            if (!checkEntryFieldsHash(r)) {
                throw new TiesDBProtocolException("ModificationEntry fields hash missmatch.");
            }
            return true;
        case RECOLLECTION_COMPUTE:
            acceptEach(session, e, this::acceptFieldList, r.computeFields::add);
            return true;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return false;
    }

    @Override
    public boolean accept(Conversation session, Event e, RecollectionResult r) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptRecollectionResult, r);
        return true;
    }

}
