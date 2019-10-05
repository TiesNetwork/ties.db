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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.reader.ChequeReader.Cheque;
import com.tiesdb.protocol.v0r0.reader.EntryHeaderReader.EntryHeader;
import com.tiesdb.protocol.v0r0.reader.FieldReader.Field;

public class EntryReader implements Reader<EntryReader.Entry> {

    public static class Entry {

        private EntryHeader header;
        private Map<String, Field> fields = new HashMap<>();
        private List<Cheque> cheques = new LinkedList<>();

        @Override
        public String toString() {
            return "Entry [header=" + header + ", fields=" + fields + "]";
        }

        public EntryHeader getHeader() {
            return header;
        }

        public Map<String, Field> getFields() {
            return fields;
        }

        public List<Cheque> getCheques() {
            return cheques;
        }

    }

    private final EntryHeaderReader entryHeaderReader = new EntryHeaderReader();
    private final FieldListReader fieldListReader = new FieldListReader();
    private final ChequeListReader chequeListReader = new ChequeListReader();

    private boolean acceptEntry(Conversation session, Event e, Entry entry) throws TiesDBProtocolException {
        switch (e.getType()) {
        case ENTRY_HEADER:
            EntryHeader header = new EntryHeader();
            boolean result = entryHeaderReader.accept(session, e, header);
            if (result) {
                if (null != entry.header) {
                    throw new TiesDBProtocolException("Multiple headers detected! Should be only one header in each entry.");
                }
                entry.header = header;
            }
            return true;
        case FIELD_LIST:
            fieldListReader.accept(session, e, (f) -> entry.fields.put(f.getName(), f));
            return true;
        case CHEQUE_LIST:
            chequeListReader.accept(session, e, entry.cheques::add);
            return true;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
            return false;
        }
    }

    @Override
    public boolean accept(Conversation session, Event e, Entry entry) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptEntry, entry);
        entry.fields = Collections.unmodifiableMap(entry.fields);
        if (!checkEntryFieldsHash(entry)) {
            throw new TiesDBProtocolException("Entry fields hash missmatch.");
        }
        return true;
    }

}
