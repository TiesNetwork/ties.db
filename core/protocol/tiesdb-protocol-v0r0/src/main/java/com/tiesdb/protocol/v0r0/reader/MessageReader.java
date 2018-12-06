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
package com.tiesdb.protocol.v0r0.reader;

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.Context.REQUEST;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.Context.RESPONSE;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.util.CheckedConsumer;

public class MessageReader implements Reader<CheckedConsumer<Reader.Message, TiesDBProtocolException>> {

    private final RequestReader requestReader = new RequestReader();
    private final ResponseReader responseReader = new ResponseReader();

    @Override
    public boolean accept(Conversation session, Event e, CheckedConsumer<Reader.Message, TiesDBProtocolException> messageHandler)
            throws TiesDBProtocolException {
        if (e.getType().getContext().is(REQUEST)) {
            requestReader.accept(session, e, new CheckedConsumer<Reader.Request, TiesDBProtocolException>() {
                @Override
                public void accept(Request v) throws TiesDBProtocolException {
                    messageHandler.accept(v);
                }
            });
            return true;
        }
        if (e.getType().getContext().is(RESPONSE)) {
            responseReader.accept(session, e, new CheckedConsumer<Reader.Response, TiesDBProtocolException>() {
                @Override
                public void accept(Response v) throws TiesDBProtocolException {
                    messageHandler.accept(v);
                }
            });
            return true;
        }
        return false;
    }

}
