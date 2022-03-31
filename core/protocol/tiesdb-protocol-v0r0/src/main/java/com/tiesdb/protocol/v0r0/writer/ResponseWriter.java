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

import static com.tiesdb.protocol.v0r0.writer.WriterUtil.write;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.writer.BillingResponseWriter.BillingResponse;
import com.tiesdb.protocol.v0r0.writer.HealingResponseWriter.HealingResponse;
import com.tiesdb.protocol.v0r0.writer.ModificationResponseWriter.ModificationResponse;
import com.tiesdb.protocol.v0r0.writer.RecollectionResponseWriter.RecollectionResponse;
import com.tiesdb.protocol.v0r0.writer.SchemaResponseWriter.SchemaResponse;
import com.tiesdb.protocol.v0r0.writer.Writer.Response.Visitor;
import com.tiesdb.protocol.v0r0.writer.WriterUtil.ConversationConsumer;
import com.tiesdb.protocol.v0r0.writer.WriterUtil.ConversationFunction;

public class ResponseWriter implements Writer<Writer.Response> {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseWriter.class);

    private static interface SpecificResponseWriter extends //
            Visitor<ConversationConsumer>, //
            ConversationFunction<Response> {
        @Override
        default ConversationConsumer accept(Response r) throws TiesDBProtocolException {
            return r.accept(this);
        }
    }

    private final SpecificResponseWriter specificResponseWriter = new SpecificResponseWriter() {

        private final ModificationResponseWriter modificationResponseWriter = new ModificationResponseWriter();
        private final RecollectionResponseWriter recollectionResponseWriter = new RecollectionResponseWriter();
        private final SchemaResponseWriter schemaResponseWriter = new SchemaResponseWriter();
        private final HealingResponseWriter healingResponseWriter = new HealingResponseWriter();
        private final BillingResponseWriter billingResponseWriter = new BillingResponseWriter();

        @Override
        public ConversationConsumer on(RecollectionResponse response) throws TiesDBProtocolException {
            return write(recollectionResponseWriter, response);
        }

        @Override
        public ConversationConsumer on(ModificationResponse response) throws TiesDBProtocolException {
            return write(modificationResponseWriter, response);
        }

        @Override
        public ConversationConsumer on(SchemaResponse response) throws TiesDBProtocolException {
            return write(schemaResponseWriter, response);
        }

        @Override
        public ConversationConsumer on(HealingResponse response) throws TiesDBProtocolException {
            return write(healingResponseWriter, response);
        }

        @Override
        public ConversationConsumer on(BillingResponse response) throws TiesDBProtocolException {
            return write(billingResponseWriter, response);
        }

    };

    @Override
    public void accept(Conversation session, Response response) throws TiesDBProtocolException {
        LOG.debug("Response {}", response);
        write(specificResponseWriter, response).accept(session);
    }

}
