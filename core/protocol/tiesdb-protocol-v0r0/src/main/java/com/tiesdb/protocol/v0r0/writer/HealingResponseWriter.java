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
import com.tiesdb.protocol.v0r0.writer.HealingResultErrorWriter.HealingResultError;
import com.tiesdb.protocol.v0r0.writer.HealingResultSuccessWriter.HealingResultSuccess;
import com.tiesdb.protocol.v0r0.writer.WriterUtil.ConversationConsumer;

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.*;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.*;

import one.utopic.sparse.ebml.format.BigIntegerFormat;

public class HealingResponseWriter implements Writer<HealingResponseWriter.HealingResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(HealingResponseWriter.class);

    public static interface HealingResponse extends Writer.Response {

        @Override
        public default <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

        Iterable<HealingResult> getResults();

    }

    public static interface HealingResult {

        interface Visitor<T> {

            T on(HealingResultError result) throws TiesDBProtocolException;

            T on(HealingResultSuccess result) throws TiesDBProtocolException;

        }

        public <T> T accept(Visitor<T> v) throws TiesDBProtocolException;

    }

    private static interface SpecificHealingResultWriter extends //
            HealingResult.Visitor<ConversationConsumer>, //
            ConversationFunction<HealingResult> {
        @Override
        default ConversationConsumer accept(HealingResult r) throws TiesDBProtocolException {
            return r.accept(this);
        }
    }

    private final SpecificHealingResultWriter specificHealingResultWriter = new SpecificHealingResultWriter() {

        private final HealingResultSuccessWriter healingSuccessWriter = new HealingResultSuccessWriter();
        private final HealingResultErrorWriter healingErrorWriter = new HealingResultErrorWriter();

        @Override
        public ConversationConsumer on(HealingResultError result) throws TiesDBProtocolException {
            return write(healingErrorWriter, result);
        }

        @Override
        public ConversationConsumer on(HealingResultSuccess result) throws TiesDBProtocolException {
            return write(healingSuccessWriter, result);
        }

    };

    @Override
    public void accept(Conversation session, HealingResponse response) throws TiesDBProtocolException {
        LOG.debug("HealingResponse {}", response);

        write(HEALING_RESPONSE, //
                write(MESSAGE_ID, BigIntegerFormat.INSTANCE, response.getMessageId()), //
                write(specificHealingResultWriter, response.getResults()) //
        ).accept(session);

    }

}
