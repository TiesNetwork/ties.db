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

import static com.tiesdb.protocol.v0r0.reader.ReaderUtil.acceptEach;
import static com.tiesdb.protocol.v0r0.reader.ReaderUtil.end;
import static network.tiesdb.util.Hex.DEFAULT_HEX;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.util.FormatUtil;

import one.utopic.sparse.ebml.format.ASCIIStringFormat;
import one.utopic.sparse.ebml.format.BytesFormat;

public class FunctionReader implements Reader<FunctionReader.Function> {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionReader.class);

    public static class Function {

        private String name;
        private List<FunctionArgument> arguments = new LinkedList<>();

        @Override
        public String toString() {
            return "Function [name=" + name + ", arguments=" + arguments + "]";
        }

        public String getName() {
            return name;
        }

        public List<FunctionArgument> getArguments() {
            return arguments;
        }

    }

    public static class ArgumentReference implements FunctionArgument {

        private String fieldName;

        @Override
        public String toString() {
            return "ArgumentReference [fieldName=" + fieldName + "]";
        }

        public String getFieldName() {
            return fieldName;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.on(this);
        }

    }

    public static class ArgumentStatic implements FunctionArgument {

        private String type;
        private byte[] rawValue;

        @Override
        public String toString() {
            return "ArgumentStatic [type=" + type + ", rawValue=" + FormatUtil.printPartialHex(rawValue) + "]";
        }

        public String getType() {
            return type;
        }

        public byte[] getRawValue() {
            return null == rawValue ? null : Arrays.copyOf(rawValue, rawValue.length);
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.on(this);
        }

    }

    public static class ArgumentFunction implements FunctionArgument {

        private Function function;

        @Override
        public String toString() {
            return "Argument" + function;
        }

        public Function getFunction() {
            return function;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.on(this);
        }

    }

    public static interface FunctionArgument {

        interface Visitor<T> {

            T on(ArgumentFunction arg);

            T on(ArgumentReference arg);

            T on(ArgumentStatic arg);

        }

        <T> T accept(Visitor<T> v);
    }

    public boolean acceptFunction(Conversation session, Event e, Function fun) throws TiesDBProtocolException {
        switch (e.getType()) {
        case FUNCTION_NAME:
            fun.name = session.read(ASCIIStringFormat.INSTANCE);
            LOG.debug("FUNCTION_NAME: {}", fun.name);
            end(session, e);
            return true;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return acceptArgument(session, e, fun);
    }

    public boolean acceptArgument(Conversation session, Event e, Function fun) throws TiesDBProtocolException {
        switch (e.getType()) {
        case FUN_ARGUMENT_REFERENCE: {
            ArgumentReference a = new ArgumentReference();
            if (acceptArgumentReference(session, e, a)) {
                LOG.debug("FUNCTION_ARG_REF: {}", a.fieldName);
                fun.arguments.add(a);
                return true;
            }
            break;
        }
        case FUN_ARGUMENT_FUNCTION: {
            LOG.debug("FUNCTION_ARG_FUN...");
            ArgumentFunction a = new ArgumentFunction();
            if (acceptArgumentFunction(session, e, a)) {
                LOG.debug("FUNCTION_ARG_FUN: {}", a);
                fun.arguments.add(a);
                return true;
            }
            break;
        }
        case FUN_ARGUMENT_STATIC: {
            LOG.debug("FUNCTION_ARG_STATIC...");
            ArgumentStatic a = new ArgumentStatic();
            acceptEach(session, e, this::acceptArgumentStatic, a);
            LOG.debug("FUNCTION_ARG_STATIC: {}", a);
            fun.arguments.add(a);
            return true;
        }
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return false;
    }

    public boolean acceptArgumentFunction(Conversation session, Event e, ArgumentFunction a) throws TiesDBProtocolException {
        Function f = new Function();
        boolean result = accept(session, e, f);
        if (result) {
            a.function = f;
        }
        return result;
    }

    public boolean acceptArgumentReference(Conversation session, Event e, ArgumentReference a) throws TiesDBProtocolException {
        a.fieldName = session.read(ASCIIStringFormat.INSTANCE);
        end(session, e);
        return true;
    }

    public boolean acceptArgumentStatic(Conversation session, Event e, ArgumentStatic a) throws TiesDBProtocolException {
        switch (e.getType()) {
        case ARG_STATIC_TYPE:
            a.type = session.read(ASCIIStringFormat.INSTANCE);
            LOG.debug("ARG_STATIC_TYPE: {}", a.type);
            end(session, e);
            return true;
        case ARG_STATIC_VALUE:
            a.rawValue = session.read(BytesFormat.INSTANCE);
            LOG.debug("ARG_STATIC_VALUE: {}", new Object() {
                @Override
                public String toString() {
                    return DEFAULT_HEX.printHexBinary(a.rawValue);
                }
            });
            end(session, e);
            return true;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return false;
    }

    @Override
    public boolean accept(Conversation session, Event e, Function fun) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptFunction, fun);
        fun.arguments = Collections.unmodifiableList(fun.arguments);
        return true;
    }

}
