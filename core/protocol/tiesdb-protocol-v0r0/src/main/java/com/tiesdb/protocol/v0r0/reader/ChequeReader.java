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

import java.math.BigInteger;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.ebml.format.UUIDFormat;
import com.tiesdb.protocol.v0r0.reader.SignatureReader.Signature;

import one.utopic.sparse.ebml.format.BigIntegerFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

public class ChequeReader implements Reader<ChequeReader.Cheque> {

    private static final Logger LOG = LoggerFactory.getLogger(ChequeReader.class);

    public static class Cheque extends Signature {

        private BigInteger chequeVersion;
        private BigInteger chequeNetwork;
        private UUID chequeSession;
        private BigInteger chequeNumber;
        private BigInteger chequeCropAmount;
        private String tablespaceName;
        private String tableName;

        public BigInteger getChequeVersion() {
            return chequeVersion;
        }

        public BigInteger getChequeNetwork() {
            return chequeNetwork;
        }

        public UUID getChequeSession() {
            return chequeSession;
        }

        public BigInteger getChequeNumber() {
            return chequeNumber;
        }

        public BigInteger getChequeCropAmount() {
            return chequeCropAmount;
        }

        public String getTablespaceName() {
            return tablespaceName;
        }

        public void setTablespaceName(String tablespaceName) {
            this.tablespaceName = tablespaceName;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        @Override
        public String toString() {
            return "Cheque [tablespaceName=" + tablespaceName + ", tableName=" + tableName + ", chequeSession=" + chequeSession
                    + ", chequeNumber=" + chequeNumber + ", chequeCropAmount=" + chequeCropAmount + ", chequeVersion=" + chequeVersion
                    + ", chequeNetwork=" + chequeNetwork + "]";
        }

    }

    private final SignatureReader signatureReader = new SignatureReader();

    public boolean acceptCheque(Conversation session, Event e, Cheque cheque) throws TiesDBProtocolException {
        switch (e.getType()) {
        case CHEQUE_VERSION:
            cheque.chequeVersion = session.read(BigIntegerFormat.INSTANCE);
            LOG.debug("CHEQUE_VERSION : {}", cheque.chequeVersion);
            end(session, e);
            return true;
        case CHEQUE_NETWORK:
            cheque.chequeNetwork = session.read(BigIntegerFormat.INSTANCE);
            LOG.debug("CHEQUE_NETWORK : {}", cheque.chequeNetwork);
            end(session, e);
            return true;
        case CHEQUE_SESSION:
            cheque.chequeSession = session.read(UUIDFormat.INSTANCE);
            LOG.debug("CHEQUE_SESSION : {}", cheque.chequeSession);
            end(session, e);
            return true;
        case CHEQUE_NUMBER:
            cheque.chequeNumber = session.read(BigIntegerFormat.INSTANCE);
            LOG.debug("CHEQUE_NUMBER : {}", cheque.chequeNumber);
            end(session, e);
            return true;
        case CHEQUE_CRP_AMOUNT:
            cheque.chequeCropAmount = session.read(BigIntegerFormat.INSTANCE);
            LOG.debug("CHEQUE_CRP_AMOUNT : {}", cheque.chequeCropAmount);
            end(session, e);
            return true;
        case TABLESPACE_NAME:
            cheque.tablespaceName = session.read(UTF8StringFormat.INSTANCE);
            LOG.debug("CHEQUE_TABLESPACE_NAME: {}", cheque.tablespaceName);
            end(session, e);
            return true;
        case TABLE_NAME:
            cheque.tableName = session.read(UTF8StringFormat.INSTANCE);
            LOG.debug("CHEQUE_TABLE_NAME: {}", cheque.tableName);
            end(session, e);
            return true;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
            return signatureReader.acceptSignature(session, e, cheque);
        }
    }

    @Override
    public boolean accept(Conversation session, Event e, Cheque cheque) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptCheque, cheque);
        return true;
    }

}
