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

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.reader.ChequeReader.Cheque;
import com.tiesdb.protocol.v0r0.util.CheckedConsumer;

public class ChequeListReader implements Reader<CheckedConsumer<ChequeReader.Cheque, TiesDBProtocolException>> {

    private final ChequeReader chequeReader = new ChequeReader();

    public boolean acceptChequeList(Conversation session, Event e,
            CheckedConsumer<ChequeReader.Cheque, TiesDBProtocolException> chequeConsumer) throws TiesDBProtocolException {
        switch (e.getType()) {
        case CHEQUE:
            Cheque cheque = new ChequeReader.Cheque();
            if (chequeReader.accept(session, e, cheque)) {
                chequeConsumer.accept(cheque);
            }
            return true;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
            return false;
        }
    }

    @Override
    public boolean accept(Conversation session, Event e, CheckedConsumer<ChequeReader.Cheque, TiesDBProtocolException> chequeConsumer)
            throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptChequeList, chequeConsumer);
        return true;
    }

}
