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

import java.util.Iterator;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.EventState;
import com.tiesdb.protocol.v0r0.ebml.TiesDBType;
import com.tiesdb.protocol.v0r0.util.CheckedConsumer;
import com.tiesdb.protocol.v0r0.util.CheckedFunction;

import one.utopic.sparse.ebml.EBMLWriter.EBMLWriteFormat;

final class WriterUtil {

    public static interface ConversationConsumer extends CheckedConsumer<Conversation, TiesDBProtocolException> {
    }

    public static interface ConversationFunction<T> extends CheckedFunction<T, ConversationConsumer, TiesDBProtocolException> {
    }

    private static final ConversationConsumer NOP_CONSUMER = s -> {
    };

    private WriterUtil() {
    }

    public static <T> ConversationConsumer write(ConversationFunction<T> f, Iterable<T> it) {
        return s -> {
            for (T t : it) {
                write(f, t).accept(s);
            }
        };
    }

    public static <T> ConversationConsumer write(ConversationFunction<T> f, T t) throws TiesDBProtocolException {
        return f.accept(t);
    }

    public static ConversationConsumer write(ConversationConsumer... cs) {
        return s -> {
            for (ConversationConsumer c : cs) {
                c.accept(s);
            }
        };
    }

    public static ConversationConsumer write(boolean condition, ConversationConsumer c) {
        return condition ? c : NOP_CONSUMER;
    }

    public static ConversationConsumer write(TiesDBType t, ConversationConsumer... cs) {
        return write(t, write(cs));
    }

    public static <T> ConversationConsumer write(Writer<T> w, T t) {
        return s -> w.accept(s, t);
    }

    public static <T> ConversationConsumer write(Writer<T> w, Iterable<T> it) {
        return write(w, it.iterator());
    }

    public static <T> ConversationConsumer write(Writer<T> w, Iterator<T> it) {
        return s -> {
            while (it.hasNext()) {
                write(w, it.next()).accept(s);
            }
        };
    }

    public static ConversationConsumer write(TiesDBType t, ConversationConsumer c) {
        return s -> writeTag(s, t, c);
    }

    public static <O> ConversationConsumer write(TiesDBType t, EBMLWriteFormat<O> f, O d) {
        return write(t, s -> writeData(s, f, d));
    }

    private static void writeTag(Conversation s, TiesDBType t, ConversationConsumer c) throws TiesDBProtocolException {
        s.accept(new Event(t, EventState.BEGIN));
        c.accept(s);
        s.accept(new Event(t, EventState.END));
    }

    private static <O> void writeData(Conversation s, EBMLWriteFormat<O> format, O data) throws TiesDBProtocolException {
        s.write(format, data);
    }
}
