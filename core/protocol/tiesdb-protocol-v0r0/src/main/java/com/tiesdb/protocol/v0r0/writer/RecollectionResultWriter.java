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

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.COMPUTE_FIELD;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.FIELD;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.FIELD_LIST;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.RECOLLECTION_COMPUTE;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.ENTRY;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.RECOLLECTION_RESULT;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.write;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.writer.EntryHeaderWriter.EntryHeader;
import com.tiesdb.protocol.v0r0.writer.FieldWriter.Field;

public class RecollectionResultWriter implements Writer<RecollectionResultWriter.RecollectionResult> {

    private static final Logger LOG = LoggerFactory.getLogger(RecollectionResultWriter.class);

    public static interface RecollectionResult {

        EntryHeader getEntryHeader();

        Multiple<Field> getEntryFields();

        Multiple<Field> getComputedFields();

    }

    private final EntryHeaderWriter entryHeaderWriter = new EntryHeaderWriter();
    private final FieldWriter entryFieldWriter = new FieldWriter(FIELD);
    private final FieldWriter computeFieldWriter = new FieldWriter(COMPUTE_FIELD);

    @Override
    public void accept(Conversation session, RecollectionResult result) throws TiesDBProtocolException {
        LOG.debug("RecollectionResult {}", result);

        Multiple<Field> computedFields = result.getComputedFields();

        write(RECOLLECTION_RESULT, //
                write(ENTRY, //
                        write(entryHeaderWriter, result.getEntryHeader()), //
                        write(FIELD_LIST, //
                                write(entryFieldWriter, result.getEntryFields()) //
                        )), //
                write(null != computedFields && !computedFields.isEmpty(), //
                        write(RECOLLECTION_COMPUTE, //
                                write(computeFieldWriter, computedFields) //
                        )//
                )//
        ).accept(session);
    }

}
