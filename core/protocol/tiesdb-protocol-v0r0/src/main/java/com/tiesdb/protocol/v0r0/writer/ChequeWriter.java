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
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.write;

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

public class ChequeWriter implements Writer<ChequeWriter.Cheque> {

    private static final Logger LOG = LoggerFactory.getLogger(ChequeWriter.class);

    public static interface Cheque extends Signature {

        BigInteger getChequeVersion();

        BigInteger getChequeNetwork();

        UUID getChequeRange();

        BigInteger getChequeNumber();

        Date getChequeTimestamp();

        BigInteger getChequeAmount();

        byte[] getHash();

        Iterable<Address> getChequeAddresses();

    }

    public static interface Address {

        byte[] getAddress();

    }

    private final SignatureWriter signatureWriter = new SignatureWriter();

    public void addressWriter(Conversation session, Address address) throws TiesDBProtocolException {
        write(ADDRESS, BytesFormat.INSTANCE, address.getAddress()).accept(session);
    }

    @Override
    public void accept(Conversation session, Cheque cheque) throws TiesDBProtocolException {
        LOG.debug("Cheque {}", cheque);
        write(CHEQUE, //
                write(CHEQUE_VERSION, BigIntegerFormat.INSTANCE, cheque.getChequeVersion()), //
                write(CHEQUE_NETWORK, BigIntegerFormat.INSTANCE, cheque.getChequeNetwork()), //
                write(CHEQUE_RANGE, UUIDFormat.INSTANCE, cheque.getChequeRange()), //
                write(CHEQUE_NUMBER, BigIntegerFormat.INSTANCE, cheque.getChequeNumber()), //
                write(CHEQUE_TIMESTAMP, DateFormat.INSTANCE, cheque.getChequeTimestamp()), //
                write(CHEQUE_AMOUNT, BigIntegerFormat.INSTANCE, cheque.getChequeAmount()), //
                write(ADDRESS_LIST, //
                        write(this::addressWriter, cheque.getChequeAddresses()) //
                ), //
                write(signatureWriter, cheque) //
        ).accept(session);
    }

}
