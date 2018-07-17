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

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.SIGNATURE;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.SIGNER;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.write;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;

import one.utopic.sparse.ebml.format.BytesFormat;

public class SignatureWriter implements Writer<SignatureWriter.Signature> {

    private static final Logger LOG = LoggerFactory.getLogger(SignatureWriter.class);

    public static interface Signature {

        public byte[] getSignature();

        public byte[] getSigner();

    }

    @Override
    public void accept(Conversation session, Signature signature) throws TiesDBProtocolException {
        LOG.debug("Signature {}", signature);
        write(//
                write(SIGNER, BytesFormat.INSTANCE, signature.getSigner()), //
                write(SIGNATURE, BytesFormat.INSTANCE, signature.getSignature()) //
        ).accept(session);
    }

}
