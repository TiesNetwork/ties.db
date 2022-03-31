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
import com.tiesdb.protocol.v0r0.writer.ChequeWriter.Cheque;
import com.tiesdb.protocol.v0r0.writer.EntryHeaderWriter.EntryHeader;
import com.tiesdb.protocol.v0r0.writer.FieldWriter.Field;

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.*;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.*;

import java.util.Iterator;

public class EntryWriter implements Writer<EntryWriter.Entry> {

    private static final Logger LOG = LoggerFactory.getLogger(EntryWriter.class);

    public static interface Entry {

        public EntryHeader getHeader();

        public Iterable<Field> getFields();

        public Iterable<Cheque> getCheques();

    }

    private final EntryHeaderWriter entryHeaderWriter = new EntryHeaderWriter();
    private final ChequeWriter chequeWriter = new ChequeWriter(false);
    private final FieldWriter fieldWriter = new FieldWriter(FIELD);

    @Override
    public void accept(Conversation session, Entry entry) throws TiesDBProtocolException {
        LOG.debug("Entry {}", entry);
        Iterator<Cheque> cheques = entry.getCheques().iterator();
        write(ENTRY, //
                write(entryHeaderWriter, entry.getHeader()), //
                write(FIELD_LIST, //
                        write(fieldWriter, entry.getFields()) //
                ), //
                writeIf(cheques.hasNext(), write(CHEQUE_LIST, //
                        write(chequeWriter, cheques)) //
                ) //
        ).accept(session);

    }

}
