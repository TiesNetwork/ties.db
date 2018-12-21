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

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.reader.ModificationResponseReader.ModificationResponse;
import com.tiesdb.protocol.v0r0.reader.RecollectionResponseReader.RecollectionResponse;
import com.tiesdb.protocol.v0r0.util.CheckedConsumer;

public class ResponseReader implements Reader<CheckedConsumer<Reader.Response, TiesDBProtocolException>> {

    private final ModificationResponseReader modificationResponseReader = new ModificationResponseReader();
    private final RecollectionResponseReader recollectionResponseReader = new RecollectionResponseReader();

    @Override
    public boolean accept(Conversation session, Event e, CheckedConsumer<Reader.Response, TiesDBProtocolException> resultHandler)
            throws TiesDBProtocolException {
        switch (e.getType()) {
        case MODIFICATION_RESPONSE: {
            ModificationResponse response = new ModificationResponse();
            if (modificationResponseReader.accept(session, e, response)) {
                resultHandler.accept(response);
                return true;
            }
            break;
        }
        case RECOLLECTION_RESPONSE: {
            RecollectionResponse response = new RecollectionResponse();
            if (recollectionResponseReader.accept(session, e, response)) {
                resultHandler.accept(response);
                return true;
            }
            break;
        }
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
        }
        return false;
    }

}
