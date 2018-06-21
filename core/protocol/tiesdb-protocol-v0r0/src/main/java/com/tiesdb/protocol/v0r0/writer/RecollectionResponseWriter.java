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

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.MESSAGE_ID;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.RECOLLECTION_RESPONSE;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.write;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.writer.RecollectionResultWriter.RecollectionResult;

import one.utopic.sparse.ebml.format.BigIntegerFormat;

public class RecollectionResponseWriter implements Writer<RecollectionResponseWriter.RecollectionResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(RecollectionResponseWriter.class);

    private final RecollectionResultWriter recollectionResultWriter = new RecollectionResultWriter();

    public static interface RecollectionResponse extends Writer.Response {

        @Override
        public default <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

        public Iterable<RecollectionResult> getResults();

    }

    @Override
    public void accept(Conversation session, RecollectionResponse response) throws TiesDBProtocolException {
        LOG.debug("RecollectionResponse {}", response);

        write(RECOLLECTION_RESPONSE, //
                write(MESSAGE_ID, BigIntegerFormat.INSTANCE, response.getMessageId()), //
                write(recollectionResultWriter, response.getResults()) //
        ).accept(session);

    }

}
