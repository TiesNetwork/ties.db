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
package com.tiesdb.protocol.v0r0.util;

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.ERROR;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.ERROR_MESSAGE;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.MESSAGE_ID;

import java.math.BigInteger;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.EventState;
import com.tiesdb.protocol.v0r0.exception.TiesDBProtocolMessageException;

import one.utopic.sparse.ebml.format.BigIntegerFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

public final class EBMLHelper {

    private static final Event MESSAGE_ID_BEGIN = new Event(MESSAGE_ID, EventState.BEGIN);
    private static final Event MESSAGE_ID_END = new Event(MESSAGE_ID, EventState.END);
    private static final Event ERROR_BEGIN = new Event(ERROR, EventState.BEGIN);
    private static final Event ERROR_END = new Event(ERROR, EventState.END);
    private static final Event ERROR_MESSAGE_BEGIN = new Event(ERROR_MESSAGE, EventState.BEGIN);
    private static final Event ERROR_MESSAGE_END = new Event(ERROR_MESSAGE, EventState.END);

    private EBMLHelper() {
    }

    public static void writeError(Conversation session, Exception e) throws TiesDBProtocolException {
        session.accept(ERROR_BEGIN);
        if (e instanceof TiesDBProtocolMessageException) {
            BigInteger messageId = ((TiesDBProtocolMessageException) e).getMessageId();
            if (null != messageId) {
                session.accept(MESSAGE_ID_BEGIN);
                session.write(BigIntegerFormat.INSTANCE, messageId);
                session.accept(MESSAGE_ID_END);
            }
        }
        for (Throwable th = e; null != th; th = th.getCause()) {
            session.accept(ERROR_MESSAGE_BEGIN);
            session.write(UTF8StringFormat.INSTANCE, th.getClass().getSimpleName() + ": " + th.getMessage());
            session.accept(ERROR_MESSAGE_END);
        }
        session.accept(ERROR_END);
    }

}
