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
import com.tiesdb.protocol.v0r0.writer.ModificationResultErrorWriter.ModificationResultError;
import com.tiesdb.protocol.v0r0.writer.ModificationResultSuccessWriter.ModificationResultSuccess;
import com.tiesdb.protocol.v0r0.writer.WriterUtil.ConversationConsumer;

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.*;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.*;

import one.utopic.sparse.ebml.format.BigIntegerFormat;

public class ModificationResponseWriter implements Writer<ModificationResponseWriter.ModificationResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(ModificationResponseWriter.class);

    public static interface ModificationResponse extends Writer.Response {

        @Override
        public default <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

        Iterable<ModificationResult> getResults();

    }

    public static interface ModificationResult {

        interface Visitor<T> {

            T on(ModificationResultError result) throws TiesDBProtocolException;

            T on(ModificationResultSuccess result) throws TiesDBProtocolException;

        }

        public <T> T accept(Visitor<T> v) throws TiesDBProtocolException;

    }

    private static interface SpecificModificationResultWriter extends //
            ModificationResult.Visitor<ConversationConsumer>, //
            ConversationFunction<ModificationResult> {
        @Override
        default ConversationConsumer accept(ModificationResult r) throws TiesDBProtocolException {
            return r.accept(this);
        }
    }

    private final SpecificModificationResultWriter specificModificationResultWriter = new SpecificModificationResultWriter() {

        private final ModificationResultSuccessWriter modificationSuccessWriter = new ModificationResultSuccessWriter();
        private final ModificationResultErrorWriter modificationErrorWriter = new ModificationResultErrorWriter();

        @Override
        public ConversationConsumer on(ModificationResultError result) throws TiesDBProtocolException {
            return write(modificationErrorWriter, result);
        }

        @Override
        public ConversationConsumer on(ModificationResultSuccess result) throws TiesDBProtocolException {
            return write(modificationSuccessWriter, result);
        }

    };

    @Override
    public void accept(Conversation session, ModificationResponse response) throws TiesDBProtocolException {
        LOG.debug("ModificationResponse {}", response);

        write(MODIFICATION_RESPONSE, //
                write(MESSAGE_ID, BigIntegerFormat.INSTANCE, response.getMessageId()), //
                write(specificModificationResultWriter, response.getResults()) //
        ).accept(session);

    }

}
