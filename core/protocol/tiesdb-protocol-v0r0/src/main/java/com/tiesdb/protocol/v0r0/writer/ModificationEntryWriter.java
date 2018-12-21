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
package com.tiesdb.protocol.v0r0.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.writer.EntryHeaderWriter.EntryHeader;
import com.tiesdb.protocol.v0r0.writer.FieldWriter.Field;

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.*;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.*;

public class ModificationEntryWriter implements Writer<ModificationEntryWriter.ModificationEntry> {

    private static final Logger LOG = LoggerFactory.getLogger(ModificationEntryWriter.class);

    public static interface ModificationEntry {

        public Iterable<Field> getFields();

        public EntryHeader getHeader();

    }

    private final EntryHeaderWriter entryHeaderWriter = new EntryHeaderWriter();
    private final FieldWriter fieldWriter = new FieldWriter(FIELD);

    @Override
    public void accept(Conversation session, ModificationEntry modificationEntry) throws TiesDBProtocolException {
        LOG.debug("ModificationEntry {}", modificationEntry);
        write(ENTRY, //
                write(entryHeaderWriter, modificationEntry.getHeader()), //
                write(FIELD_LIST, //
                        write(fieldWriter, modificationEntry.getFields()) //
                ) //
        ).accept(session);

    }

}
