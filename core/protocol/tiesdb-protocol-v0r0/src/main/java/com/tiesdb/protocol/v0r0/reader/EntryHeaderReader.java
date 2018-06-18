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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.function.Consumer;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.lib.crypto.digest.DigestManager;
import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;
import com.tiesdb.protocol.v0r0.reader.SignatureReader.Signature;

import com.tiesdb.protocol.v0r0.util.FormatUtil;
import one.utopic.sparse.ebml.format.BytesFormat;
import one.utopic.sparse.ebml.format.DateFormat;
import one.utopic.sparse.ebml.format.IntegerFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

public class EntryHeaderReader implements Reader<EntryHeaderReader.EntryHeader> {

    private static final Logger LOG = LoggerFactory.getLogger(EntryHeaderReader.class);

    public static class EntryHeader {

        private String tablespaceName;
        private String tableName;
        private Integer entryType;
        private Date entryTimestamp;
        private Integer entryVersion;
        private Integer entryNetwork;
        private byte[] entryOldHash;
        private byte[] entryFldHash;
        private byte[] rawBytes;

        private byte[] headerHash;
        private final Signature signature = new Signature();

        @Override
        public String toString() {
            return "EntryHeader [tablespaceName=" + tablespaceName + ", tableName=" + tableName + ", entryType=" + entryType
                    + ", entryTimestamp=" + entryTimestamp + ", entryVersion=" + entryVersion + ", entryNetwork=" + entryNetwork
                    + ", entryOldHash=" + FormatUtil.printHex(entryOldHash) + ", entryFldHash=" + FormatUtil.printHex(entryFldHash)
                    + ", rawBytes=" + FormatUtil.printHex(rawBytes) + ", signature=" + signature + "]";
        }

        public String getTablespaceName() {
            return tablespaceName;
        }

        public String getTableName() {
            return tableName;
        }

        public Integer getEntryType() {
            return entryType;
        }

        public Date getEntryTimestamp() {
            return entryTimestamp;
        }

        public Integer getEntryVersion() {
            return entryVersion;
        }

        public Integer getEntryNetwork() {
            return entryNetwork;
        }

        public byte[] getEntryOldHash() {
            return entryOldHash;
        }

        public byte[] getEntryFldHash() {
            return entryFldHash;
        }

        public byte[] getHeaderHash() {
            return headerHash;
        }

        public Signature getSignature() {
            return signature;
        }

        public byte[] getRawBytes() {
            return rawBytes;
        }

    }

    private final SignatureReader signatureReader = new SignatureReader(() -> getDC().getFieldHashListener());
    private final ThreadLocal<DigestCalculator> digestCalculator = new ThreadLocal<>();

    public boolean acceptEntryHeader(Conversation session, Event e, EntryHeader header) throws TiesDBProtocolException {
        switch (e.getType()) {
        case ENTRY_TABLESPACE_NAME:
            header.tablespaceName = session.read(UTF8StringFormat.INSTANCE);
            LOG.debug("ENTRY_TABLESPACE_NAME: {}", header.tablespaceName);
            end(session, e);
            return true;
        case ENTRY_TABLE_NAME:
            header.tableName = session.read(UTF8StringFormat.INSTANCE);
            LOG.debug("ENTRY_TABLE_NAME: {}", header.tableName);
            end(session, e);
            return true;
        case ENTRY_TYPE:
            header.entryType = session.read(IntegerFormat.INSTANCE);
            LOG.debug("ENTRY_TYPE: {}", header.entryType);
            end(session, e);
            return true;
        case ENTRY_TIMESTAMP:
            header.entryTimestamp = session.read(DateFormat.INSTANCE);
            LOG.debug("ENTRY_TIMESTAMP: {}", header.entryTimestamp);
            end(session, e);
            return true;
        case ENTRY_VERSION:
            header.entryVersion = session.read(IntegerFormat.INSTANCE);
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
                    return DatatypeConverter.printHexBinary(header.entryOldHash);
                }
            });
            end(session, e);
            return true;
        case ENTRY_FLD_HASH:
            header.entryFldHash = session.read(BytesFormat.INSTANCE);
            LOG.debug("ENTRY_FLD_HASH: {}", new Object() {
                @Override
                public String toString() {
                    return DatatypeConverter.printHexBinary(header.entryFldHash);
                }
            });
            end(session, e);
            return true;
        // $CASES-OMITTED$
        default:
            return signatureReader.accept(session, e, header.signature);
        // throw new TiesDBProtocolException("Illegal packet format");
        }
    }

    @Override
    public boolean accept(Conversation session, Event e, EntryHeader header) throws TiesDBProtocolException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Consumer<Byte> rawBytesListener = baos::write;
            session.addReaderListener(rawBytesListener);
            DigestCalculator dc = getDC(true);
            Digest headerDigest = dc.getFieldDigest();
            Consumer<Byte> headerHashListener = dc.getFieldHashListener();
            try {
                headerDigest.reset();
                session.addReaderListener(headerHashListener);
                acceptEach(session, e, this::acceptEntryHeader, header);
                byte[] headerHash = new byte[headerDigest.getDigestSize()];
                if (headerDigest.getDigestSize() == headerDigest.doFinal(headerHash, 0)) {
                    LOG.debug("ENTRY_HASH: {}", new Object() {
                        @Override
                        public String toString() {
                            return DatatypeConverter.printHexBinary(headerHash);
                        }
                    });
                    header.headerHash = headerHash;
                    if (!checkSignature(headerHash, header.signature)) {
                        throw new TiesDBProtocolException("Header signature check failed.");
                    }
                } else {
                    throw new TiesDBProtocolException("Header digest failed to compute headerHash");
                }
            } finally {
                session.removeReaderListener(headerHashListener);
            }
            header.rawBytes = baos.toByteArray();
        } catch (IOException ex) {
            throw new TiesDBProtocolException(ex);
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
