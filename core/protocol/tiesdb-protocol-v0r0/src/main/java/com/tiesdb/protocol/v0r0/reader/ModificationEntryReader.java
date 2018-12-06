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

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.reader.EntryHeaderReader.EntryHeader;
import com.tiesdb.protocol.v0r0.reader.FieldReader.Field;

public class ModificationEntryReader implements Reader<ModificationEntryReader.ModificationEntry> {

    public static class ModificationEntry implements Entry {

        private EntryHeader header;
        private HashMap<String, Field> fields = new HashMap<>();

        @Override
        public String toString() {
            return "ModificationEntry [header=" + header + ", fields=" + fields + "]";
        }

        @Override
        public EntryHeader getHeader() {
            return header;
        }

        @Override
        public HashMap<String, Field> getFields() {
            return fields;
        }

    }

    private final EntryHeaderReader entryHeaderReader = new EntryHeaderReader();
    private final FieldReader fieldReader = new FieldReader();

    public boolean acceptFieldList(Conversation session, Event e, HashMap<String, Field> t) throws TiesDBProtocolException {
        switch (e.getType()) {
        case FIELD:
            Field f = new Field();
            if (fieldReader.accept(session, e, f)) {
                t.put(f.getName(), f);
                return true;
            }
            break;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return false;
    }

    private boolean acceptEntry(Conversation session, Event e, ModificationEntry modificationEntry) throws TiesDBProtocolException {
        switch (e.getType()) {
        case ENTRY_HEADER:
            EntryHeader header = new EntryHeader();
            boolean result = entryHeaderReader.accept(session, e, header);
            if (result) {
                if (null != modificationEntry.header) {
                    throw new TiesDBProtocolException("Multiple headers detected! Should be only one header in each entry.");
                }
                modificationEntry.header = header;
            }
            return true;
        case FIELD_LIST:
            acceptEach(session, e, this::acceptFieldList, modificationEntry.fields);
            return true;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return false;
    }

    @Override
    public boolean accept(Conversation session, Event e, ModificationEntry modificationEntry) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptEntry, modificationEntry);
        if (!checkEntryFieldsHash(modificationEntry)) {
            throw new TiesDBProtocolException("ModificationEntry fields hash missmatch.");
        }
        return true;
    }

}
