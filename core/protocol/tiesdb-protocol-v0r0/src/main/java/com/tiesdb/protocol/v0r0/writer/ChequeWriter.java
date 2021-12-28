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

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.*;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.*;

import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.ebml.format.UUIDFormat;
import com.tiesdb.protocol.v0r0.writer.SignatureWriter.Signature;

import one.utopic.sparse.ebml.format.BigIntegerFormat;
import one.utopic.sparse.ebml.format.BytesFormat;
import one.utopic.sparse.ebml.format.DateFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

public class ChequeWriter implements Writer<ChequeWriter.Cheque> {

    private static final Logger LOG = LoggerFactory.getLogger(ChequeWriter.class);

    public static interface Cheque extends Signature {

        BigInteger getChequeVersion();

        BigInteger getChequeNetwork();

        String getTablespaceName();

        String getTableName();

        UUID getChequeSession();

        BigInteger getChequeNumber();

        BigInteger getChequeCropAmount();

    }

    private final SignatureWriter signatureWriter = new SignatureWriter(false);

    @Override
    public void accept(Conversation session, Cheque cheque) throws TiesDBProtocolException {
        LOG.debug("Cheque {}", cheque);
        write(CHEQUE, //
                write(CHEQUE_VERSION, BigIntegerFormat.INSTANCE, cheque.getChequeVersion()), //
                writeNotNull(CHEQUE_NETWORK, BigIntegerFormat.INSTANCE, cheque.getChequeNetwork()), //
                write(CHEQUE_SESSION, UUIDFormat.INSTANCE, cheque.getChequeSession()), //
                write(CHEQUE_NUMBER, BigIntegerFormat.INSTANCE, cheque.getChequeNumber()), //
                write(CHEQUE_CRP_AMOUNT, BigIntegerFormat.INSTANCE, cheque.getChequeCropAmount()), //
                writeNotNull(TABLESPACE_NAME, UTF8StringFormat.INSTANCE, cheque.getTablespaceName()), //
                writeNotNull(TABLE_NAME, UTF8StringFormat.INSTANCE, cheque.getTableName()), //
                write(signatureWriter, cheque) //
        ).accept(session);
    }

}
