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
import com.tiesdb.protocol.v0r0.writer.ModificationRequestWriter.ModificationRequest;
import com.tiesdb.protocol.v0r0.writer.RecollectionRequestWriter.RecollectionRequest;
//import com.tiesdb.protocol.v0r0.writer.SchemaRequestWriter.SchemaRequest;
import com.tiesdb.protocol.v0r0.writer.Writer.Request.Visitor;
import com.tiesdb.protocol.v0r0.writer.WriterUtil.ConversationConsumer;
import com.tiesdb.protocol.v0r0.writer.WriterUtil.ConversationFunction;

public class RequestWriter implements Writer<Writer.Request> {

    private static final Logger LOG = LoggerFactory.getLogger(RequestWriter.class);

    private static interface SpecificRequestWriter extends //
            Visitor<ConversationConsumer>, //
            ConversationFunction<Request> {
        @Override
        default ConversationConsumer accept(Request r) throws TiesDBProtocolException {
            return r.accept(this);
        }
    }

    private final SpecificRequestWriter specificRequestWriter = new SpecificRequestWriter() {

        private final ModificationRequestWriter modificationRequestWriter = new ModificationRequestWriter();
        private final RecollectionRequestWriter recollectionRequestWriter = new RecollectionRequestWriter();
        //private final SchemaRequestWriter schemaRequestWriter = new SchemaRequestWriter();

        @Override
        public ConversationConsumer on(ModificationRequest request) throws TiesDBProtocolException {
            return write(modificationRequestWriter, request);
        }

        @Override
        public ConversationConsumer on(RecollectionRequest request) throws TiesDBProtocolException {
            return write(recollectionRequestWriter, request);
        }

//        @Override
//        public ConversationConsumer on(SchemaRequest response) throws TiesDBProtocolException {
//            return write(schemaRequestWriter, response);
//        }
    };

    @Override
    public void accept(Conversation session, Request response) throws TiesDBProtocolException {
        LOG.debug("Request {}", response);

        write(specificRequestWriter, response).accept(session);
    }

}
