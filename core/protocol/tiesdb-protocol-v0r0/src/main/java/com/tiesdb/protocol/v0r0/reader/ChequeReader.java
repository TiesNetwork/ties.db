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

import static com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.DEFAULT_DIGEST_ALG;
import static com.tiesdb.protocol.v0r0.reader.ReaderUtil.acceptEach;
import static com.tiesdb.protocol.v0r0.reader.ReaderUtil.checkSignature;
import static com.tiesdb.protocol.v0r0.reader.ReaderUtil.end;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static network.tiesdb.util.Hex.DEFAULT_HEX;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.lib.crypto.digest.DigestManager;
import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.ebml.format.UUIDFormat;
import com.tiesdb.protocol.v0r0.reader.SignatureReader.Signature;
import com.tiesdb.protocol.v0r0.util.FormatUtil;

import one.utopic.sparse.ebml.format.BigIntegerFormat;
import one.utopic.sparse.ebml.format.BytesFormat;
import one.utopic.sparse.ebml.format.DateFormat;

public class ChequeReader implements Reader<ChequeReader.Cheque> {

    private static final Logger LOG = LoggerFactory.getLogger(ChequeReader.class);

    public static class Cheque extends Signature {

        private BigInteger chequeVersion;
        private BigInteger chequeNetwork;
        private UUID chequeRange;
        private BigInteger chequeNumber;
        private Date chequeTimestamp;
        private BigInteger chequeAmount;
        private byte[] hash;
        private List<Address> chequeAddresses = new LinkedList<>();

        public BigInteger getChequeVersion() {
            return chequeVersion;
        }

        public BigInteger getChequeNetwork() {
            return chequeNetwork;
        }

        public UUID getChequeRange() {
            return chequeRange;
        }

        public BigInteger getChequeNumber() {
            return chequeNumber;
        }

        public Date getChequeTimestamp() {
            return chequeTimestamp;
        }

        public BigInteger getChequeAmount() {
            return chequeAmount;
        }

        public byte[] getHash() {
            return null == hash ? null : Arrays.copyOf(hash, hash.length);
        }

        public List<Address> getChequeAddresses() {
            return chequeAddresses;
        }

        @Override
        public String toString() {
            return "Cheque [chequeRange=" + chequeRange + ", chequeNumber=" + chequeNumber + ", chequeTimestamp=" + chequeTimestamp
                    + ", chequeAmount=" + chequeAmount + ", chequeVersion=" + chequeVersion + ", chequeNetwork=" + chequeNetwork
                    + ", chequeAddresses=" + chequeAddresses + ", signature=" + super.toString() + "]";
        }

    }

    public static class Address {

        private final byte[] address;

        public Address(byte[] address) {
            super();
            this.address = address;
        }

        public byte[] getAddress() {
            return null == address ? null : Arrays.copyOf(address, address.length);
        }

        @Override
        public String toString() {
            return "Address [address=" + FormatUtil.printPartialHex(address) + "]";
        }

    }

    private final SignatureReader signatureReader = new SignatureReader(() -> getDC().getHashListener());
    private final ThreadLocal<DigestCalculator> digestCalculator = new ThreadLocal<>();

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
        case CHEQUE_RANGE:
            cheque.chequeRange = session.read(UUIDFormat.INSTANCE);
            LOG.debug("CHEQUE_RANGE : {}", cheque.chequeRange);
            end(session, e);
            return true;
        case CHEQUE_NUMBER:
            cheque.chequeNumber = session.read(BigIntegerFormat.INSTANCE);
            LOG.debug("CHEQUE_NUMBER : {}", cheque.chequeNumber);
            end(session, e);
            return true;
        case CHEQUE_TIMESTAMP:
            cheque.chequeTimestamp = session.read(DateFormat.INSTANCE);
            LOG.debug("CHEQUE_TIMESTAMP : {}", cheque.chequeTimestamp);
            end(session, e);
            return true;
        case CHEQUE_AMOUNT:
            cheque.chequeAmount = session.read(BigIntegerFormat.INSTANCE);
            LOG.debug("CHEQUE_AMOUNT : {}", cheque.chequeAmount);
            end(session, e);
            return true;
        case ADDRESS_LIST:
            acceptEach(session, e, this::acceptAddressList, cheque.chequeAddresses);
            return true;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
            return signatureReader.acceptSignature(session, e, cheque);
        }
    }

    public boolean acceptAddressList(Conversation session, Event e, List<Address> addresses) throws TiesDBProtocolException {
        switch (e.getType()) {
        case ADDRESS:
            byte[] bytes = session.read(BytesFormat.INSTANCE);
            LOG.debug("ADDRESS : {}", new Object() {
                @Override
                public String toString() {
                    return DEFAULT_HEX.printHexBinary(bytes);
                }
            });
            end(session, e);
            if (null != bytes && bytes.length > 0) {
                addresses.add(new Address(bytes));
            } else {
                LOG.error("Skipping illegal address: {}", bytes);
            }
            return true;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
            return false;
        }
    }

    @Override
    public boolean accept(Conversation session, Event e, Cheque cheque) throws TiesDBProtocolException {
        DigestCalculator dc = getDC(true);
        Digest chequeDigest = dc.getDigest();
        Consumer<Byte> chequeHashListener = dc.getHashListener();
        try {
            chequeDigest.reset();
            session.addReaderListener(chequeHashListener);
            acceptEach(session, e, this::acceptCheque, cheque);
            Collections.unmodifiableList(cheque.chequeAddresses);
            byte[] chequeHash = new byte[chequeDigest.getDigestSize()];
            if (chequeDigest.getDigestSize() == chequeDigest.doFinal(chequeHash, 0)) {
                LOG.debug("CHEQUE_HASH: {}", new Object() {
                    @Override
                    public String toString() {
                        return DEFAULT_HEX.printHexBinary(chequeHash);
                    }
                });
                cheque.hash = chequeHash;
                if (!checkSignature(chequeHash, cheque)) {
                    throw new TiesDBProtocolException("Cheque signature check failed.");
                }
            } else {
                throw new TiesDBProtocolException("Cheque digest failed to compute chequeHash");
            }
        } finally {
            session.removeReaderListener(chequeHashListener);
        }
        return true;
    }

    private DigestCalculator getDC() {
        return getDC(false);
    }

    private DigestCalculator getDC(boolean autocreate) {
        DigestCalculator dc;
        if (null == (dc = digestCalculator.get())) {
            if (autocreate) {
                dc = new DigestCalculator(DigestManager.getDigest(DEFAULT_DIGEST_ALG));
                digestCalculator.set(dc);
            } else {
                throw new IllegalStateException("No DigestCalculator found");
            }
        }
        return dc;
    }

}
