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

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.ENTRY_FLD_HASH;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.ENTRY_OLD_HASH;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.ENTRY_HEADER;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.ENTRY_NETWORK;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.ENTRY_TABLESPACE_NAME;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.ENTRY_TABLE_NAME;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.ENTRY_TIMESTAMP;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.ENTRY_VERSION;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.write;

import java.math.BigInteger;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.writer.SignatureWriter.Signature;

import one.utopic.sparse.ebml.format.BigIntegerFormat;
import one.utopic.sparse.ebml.format.BytesFormat;
import one.utopic.sparse.ebml.format.DateFormat;
import one.utopic.sparse.ebml.format.IntegerFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

public class EntryHeaderWriter implements Writer<EntryHeaderWriter.EntryHeader> {

    private static final Logger LOG = LoggerFactory.getLogger(EntryHeaderWriter.class);

    public static interface EntryHeader extends Signature {

        public String getTablespaceName();

        public String getTableName();

        public byte[] getEntryFldHash();

        public Integer getEntryNetwork();

        public byte[] getEntryOldHash();

        public Date getEntryTimestamp();

        public BigInteger getEntryVersion();

    }

    private final SignatureWriter signatureWriter = new SignatureWriter();

    @Override
    public void accept(Conversation session, EntryHeader header) throws TiesDBProtocolException {
        LOG.debug("EntryHeader {}", header);
        byte[] entryOldHash = header.getEntryOldHash();
        write(ENTRY_HEADER, //
                write(ENTRY_TABLESPACE_NAME, UTF8StringFormat.INSTANCE, header.getTablespaceName()), //
                write(ENTRY_TABLE_NAME, UTF8StringFormat.INSTANCE, header.getTableName()), //
                write(ENTRY_TIMESTAMP, DateFormat.INSTANCE, header.getEntryTimestamp()), //
                write(ENTRY_VERSION, BigIntegerFormat.INSTANCE, header.getEntryVersion()), //
                write(null != entryOldHash && 0 != entryOldHash.length, //
                        write(ENTRY_OLD_HASH, BytesFormat.INSTANCE, entryOldHash) //
                ), //
                write(ENTRY_FLD_HASH, BytesFormat.INSTANCE, header.getEntryFldHash()), //
                write(ENTRY_NETWORK, IntegerFormat.INSTANCE, header.getEntryNetwork()), //
                write(signatureWriter, header) //
        ).accept(session);
    }

}
