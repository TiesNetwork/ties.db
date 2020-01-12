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
import com.tiesdb.protocol.v0r0.writer.RecollectionErrorWriter.RecollectionError;
import com.tiesdb.protocol.v0r0.writer.RecollectionResultWriter.RecollectionEntry;
import com.tiesdb.protocol.v0r0.writer.WriterUtil.ConversationConsumer;
import com.tiesdb.protocol.v0r0.writer.WriterUtil.ConversationFunction;

import one.utopic.sparse.ebml.format.BigIntegerFormat;

public class RecollectionResponseWriter implements Writer<RecollectionResponseWriter.RecollectionResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(RecollectionResponseWriter.class);

    public static interface RecollectionResponse extends Writer.Response {

        @Override
        public default <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

        public Iterable<RecollectionResult> getResults();

    }

    public static interface RecollectionResult {

        interface Visitor<T> {

            T on(RecollectionEntry entry) throws TiesDBProtocolException;

            T on(RecollectionError error) throws TiesDBProtocolException;

        }

        <T> T accept(Visitor<T> v) throws TiesDBProtocolException;

    }

    private static interface SpecificRecollectionResultWriter extends //
            RecollectionResult.Visitor<ConversationConsumer>, //
            ConversationFunction<RecollectionResult> {
        @Override
        default ConversationConsumer accept(RecollectionResult r) throws TiesDBProtocolException {
            return r.accept(this);
        }
    }

    private final SpecificRecollectionResultWriter specificRecollectionResultWriter = new SpecificRecollectionResultWriter() {

        private final RecollectionResultWriter recollectionResultWriter = new RecollectionResultWriter();
        private final RecollectionErrorWriter recollectionErrorWriter = new RecollectionErrorWriter();

        @Override
        public ConversationConsumer on(RecollectionEntry result) throws TiesDBProtocolException {
            return write(recollectionResultWriter, result);
        }

        @Override
        public ConversationConsumer on(RecollectionError result) throws TiesDBProtocolException {
            return write(recollectionErrorWriter, result);
        }

    };

    @Override
    public void accept(Conversation session, RecollectionResponse response) throws TiesDBProtocolException {
        LOG.debug("RecollectionResponse {}", response);

        write(RECOLLECTION_RESPONSE, //
                write(MESSAGE_ID, BigIntegerFormat.INSTANCE, response.getMessageId()), //
                write(specificRecollectionResultWriter, response.getResults()) //
        ).accept(session);

    }

}
