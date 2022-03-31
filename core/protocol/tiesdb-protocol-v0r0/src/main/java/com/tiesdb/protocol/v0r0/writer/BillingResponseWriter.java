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

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.BILLING_RESPONSE;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.CHEQUE_LIST;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.MESSAGE_ID;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.write;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.writeIf;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.writer.ChequeWriter.Cheque;

import one.utopic.sparse.ebml.format.BigIntegerFormat;

public class BillingResponseWriter implements Writer<BillingResponseWriter.BillingResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(BillingResponseWriter.class);

    public static interface BillingResponse extends Writer.Response {

        @Override
        public default <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
            return v.on(this);
        }

        public Iterable<Cheque> getCheques();

    }

    ChequeWriter chequeWriter = new ChequeWriter();

    public void acceptCheque(Conversation session, Cheque cheque) throws TiesDBProtocolException {
        chequeWriter.accept(session, cheque);
    }

    @Override
    public void accept(Conversation session, BillingResponse response) throws TiesDBProtocolException {
        LOG.debug("BillingResponse {}", response);
        Iterator<Cheque> cheques = response.getCheques().iterator();
        write(BILLING_RESPONSE, //
                write(MESSAGE_ID, BigIntegerFormat.INSTANCE, response.getMessageId()), // , //
                writeIf(cheques.hasNext(), write(CHEQUE_LIST, //
                        write(chequeWriter, cheques)) //
                ) //
        ).accept(session);
    }

}
