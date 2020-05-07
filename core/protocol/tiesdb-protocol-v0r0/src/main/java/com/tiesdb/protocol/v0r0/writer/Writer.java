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

import java.math.BigInteger;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.writer.HealingRequestWriter.HealingRequest;
import com.tiesdb.protocol.v0r0.writer.HealingResponseWriter.HealingResponse;
import com.tiesdb.protocol.v0r0.writer.ModificationRequestWriter.ModificationRequest;
import com.tiesdb.protocol.v0r0.writer.ModificationResponseWriter.ModificationResponse;
import com.tiesdb.protocol.v0r0.writer.RecollectionRequestWriter.RecollectionRequest;
import com.tiesdb.protocol.v0r0.writer.RecollectionResponseWriter.RecollectionResponse;
import com.tiesdb.protocol.v0r0.writer.SchemaResponseWriter.SchemaResponse;

@FunctionalInterface
public interface Writer<T> {

    interface Message {

        BigInteger getMessageId();

    }

    interface Request extends Message {

        interface Visitor<T> {

            T on(ModificationRequest modificationRequest) throws TiesDBProtocolException;

            T on(RecollectionRequest recollectionRequest) throws TiesDBProtocolException;

            T on(HealingRequest healingRequest) throws TiesDBProtocolException;

        }

        <T> T accept(Visitor<T> v) throws TiesDBProtocolException;
    }

    interface Response extends Message {

        interface Visitor<T> {

            T on(ModificationResponse modificationResponse) throws TiesDBProtocolException;

            T on(RecollectionResponse recollectionResponse) throws TiesDBProtocolException;

            T on(SchemaResponse schemaResponse) throws TiesDBProtocolException;

            T on(HealingResponse healingResponse) throws TiesDBProtocolException;

        }

        <T> T accept(Visitor<T> v) throws TiesDBProtocolException;

    }

    void accept(Conversation session, T t) throws TiesDBProtocolException;

}
