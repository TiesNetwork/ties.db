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
import static network.tiesdb.util.Hex.DEFAULT_HEX;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.lib.crypto.digest.DigestManager;
import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.reader.SignatureReader.Signature;
import com.tiesdb.protocol.v0r0.util.FormatUtil;

import one.utopic.sparse.ebml.format.BigIntegerFormat;
import one.utopic.sparse.ebml.format.BytesFormat;
import one.utopic.sparse.ebml.format.DateFormat;
import one.utopic.sparse.ebml.format.IntegerFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

public class EntryHeaderReader implements Reader<EntryHeaderReader.EntryHeader> {

    private static final Logger LOG = LoggerFactory.getLogger(EntryHeaderReader.class);

    public static class EntryHeader extends Signature {

        private String tablespaceName;
        private String tableName;
        private Date entryTimestamp;
        private BigInteger entryVersion;
        private Integer entryNetwork;
        private byte[] entryOldHash;
        private byte[] entryFldHash;
        private byte[] hash;

        @Override
        public String toString() {
            return "EntryHeader [tablespaceName=" + tablespaceName + ", tableName=" + tableName /* + ", entryType=" + entryType */
                    + ", entryTimestamp=" + entryTimestamp + ", entryVersion=" + entryVersion + ", entryNetwork=" + entryNetwork
                    + ", entryOldHash=" + FormatUtil.printPartialHex(entryOldHash) + ", entryFldHash="
                    + FormatUtil.printPartialHex(entryFldHash) + ", hash=" + FormatUtil.printPartialHex(hash) + ", signature="
                    + super.toString() + "]";
        }

        public String getTablespaceName() {
            return tablespaceName;
        }

        public String getTableName() {
            return tableName;
        }

        public Date getEntryTimestamp() {
            return entryTimestamp;
        }

        public BigInteger getEntryVersion() {
            return entryVersion;
        }

        public Integer getEntryNetwork() {
            return entryNetwork;
        }

        public byte[] getEntryOldHash() {
            return null == entryOldHash ? null : Arrays.copyOf(entryOldHash, entryOldHash.length);
        }

        public byte[] getEntryFldHash() {
            return null == entryFldHash ? null : Arrays.copyOf(entryFldHash, entryFldHash.length);
        }

        public byte[] getHash() {
            return null == hash ? null : Arrays.copyOf(hash, hash.length);
        }

    }

    private final SignatureReader signatureReader = new SignatureReader(() -> getDC().getHashListener());
    private final ThreadLocal<DigestCalculator> digestCalculator = new ThreadLocal<>();

    public boolean acceptEntryHeader(Conversation session, Event e, EntryHeader header) throws TiesDBProtocolException {
        switch (e.getType()) {
        case TABLESPACE_NAME:
            header.tablespaceName = session.read(UTF8StringFormat.INSTANCE);
            LOG.debug("ENTRY_TABLESPACE_NAME: {}", header.tablespaceName);
            end(session, e);
            return true;
        case TABLE_NAME:
            header.tableName = session.read(UTF8StringFormat.INSTANCE);
            LOG.debug("ENTRY_TABLE_NAME: {}", header.tableName);
            end(session, e);
            return true;
        case ENTRY_TIMESTAMP:
            header.entryTimestamp = session.read(DateFormat.INSTANCE);
            LOG.debug("ENTRY_TIMESTAMP: {}", header.entryTimestamp);
            end(session, e);
            return true;
        case ENTRY_VERSION:
            header.entryVersion = session.read(BigIntegerFormat.INSTANCE);
            LOG.debug("ENTRY_VERSION: {}", header.entryVersion);
            end(session, e);
            return true;
        case ENTRY_NETWORK:
            header.entryNetwork = session.read(IntegerFormat.INSTANCE);
            LOG.debug("ENTRY_NETWORK: {}", header.entryNetwork);
            end(session, e);
            return true;
        case ENTRY_OLD_HASH:
            header.entryOldHash = session.read(BytesFormat.INSTANCE);
            LOG.debug("ENTRY_OLD_HASH: {}", new Object() {
                @Override
                public String toString() {
                    return DEFAULT_HEX.printHexBinary(header.entryOldHash);
                }
            });
            end(session, e);
            return true;
        case ENTRY_FLD_HASH:
            header.entryFldHash = session.read(BytesFormat.INSTANCE);
            LOG.debug("ENTRY_FLD_HASH: {}", new Object() {
                @Override
                public String toString() {
                    return DEFAULT_HEX.printHexBinary(header.entryFldHash);
                }
            });
            end(session, e);
            return true;
        // $CASES-OMITTED$
        default:
            // throw new TiesDBProtocolException("Illegal packet format");
            return signatureReader.acceptSignature(session, e, header);
        }
    }

    @Override
    public boolean accept(Conversation session, Event e, EntryHeader header) throws TiesDBProtocolException {
        DigestCalculator dc = getDC(true);
        Digest headerDigest = dc.getDigest();
        Consumer<Byte> headerHashListener = dc.getHashListener();
        try {
            headerDigest.reset();
            session.addReaderListener(headerHashListener);
            acceptEach(session, e, this::acceptEntryHeader, header);
            byte[] headerHash = new byte[headerDigest.getDigestSize()];
            if (headerDigest.getDigestSize() == headerDigest.doFinal(headerHash, 0)) {
                LOG.debug("ENTRY_HEADER_HASH: {}", new Object() {
                    @Override
                    public String toString() {
                        return DEFAULT_HEX.printHexBinary(headerHash);
                    }
                });
                header.hash = headerHash;
                if (!checkSignature(headerHash, header)) {
                    throw new TiesDBProtocolException("Header signature check failed.");
                }
            } else {
                throw new TiesDBProtocolException("Header digest failed to compute headerHash");
            }
        } finally {
            session.removeReaderListener(headerHashListener);
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
