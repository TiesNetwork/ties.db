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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.reader.FunctionReader.Function;

import one.utopic.sparse.ebml.format.UTF8StringFormat;

public class FilterReader implements Reader<FilterReader.Filter> {

    private static final Logger LOG = LoggerFactory.getLogger(FilterReader.class);

    public static class Filter extends Function {

        private String fieldName;

        @Override
        public String toString() {
            return "Filter [fieldName=" + fieldName + ", function=" + super.toString() + "]";
        }

        public String getFieldName() {
            return fieldName;
        }

    }

    private final FunctionReader functionReader = new FunctionReader();

    public boolean acceptFilter(Conversation session, Event e, Filter f) throws TiesDBProtocolException {
        switch (e.getType()) {
        case FILTER_FIELD:
            f.fieldName = session.read(UTF8StringFormat.INSTANCE);
            LOG.debug("FILTER_FIELD: {}", f.fieldName);
            end(session, e);
            return true;
        // $CASES-OMITTED$
        default:
            return functionReader.acceptFunction(session, e, f);
        }
    }

    @Override
    public boolean accept(Conversation session, Event e, Filter f) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptFilter, f);
        return true;
    }

}
