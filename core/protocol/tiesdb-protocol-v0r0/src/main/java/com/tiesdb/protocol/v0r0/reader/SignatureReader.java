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
import static network.tiesdb.util.Hex.UPPERCASE_HEX;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation.Event;

import com.tiesdb.protocol.v0r0.util.FormatUtil;
import one.utopic.sparse.ebml.format.BytesFormat;

public class SignatureReader implements Reader<SignatureReader.Signature> {

    @FunctionalInterface
    private static interface Procedure {
        void process() throws TiesDBProtocolException;
    }

    @FunctionalInterface
    private static interface Processor<T, U> {
        void accept(T t, U u) throws TiesDBProtocolException;
    }

    public static class Signature {

        private byte[] signature;
        private byte[] signer;

        public byte[] getSignature() {
            return null == signature ? null : Arrays.copyOf(signature, signature.length);
        }

        public byte[] getSigner() {
            return null == signer ? null : Arrays.copyOf(signer, signer.length);
        }

        @Override
        public String toString() {
            return "Signature [signer=" + FormatUtil.printPartialHex(signer) + ", signature=" + FormatUtil.printPartialHex(signature) + "]";
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(SignatureReader.class);
    private final Processor<Conversation, Procedure> hashListenerWrapper;

    public SignatureReader() {
        this(null);
    }

    public SignatureReader(Supplier<Consumer<Byte>> hashListenerSupplier) {
        if (null == hashListenerSupplier) {
            this.hashListenerWrapper = (session, fun) -> fun.process();
        } else {
            this.hashListenerWrapper = (session, fun) -> {
                Consumer<Byte> hashListener = hashListenerSupplier.get();
                session.removeReaderListener(hashListener);
                fun.process();
                session.addReaderListener(hashListener);
            };
        }
    }

    public boolean acceptSignature(Conversation session, Event e, Signature signature) throws TiesDBProtocolException {
        switch (e.getType()) {
        case SIGNATURE:
            this.hashListenerWrapper.accept(session, () -> {
                signature.signature = session.read(BytesFormat.INSTANCE);
            });
            LOG.debug("SIGNATURE : {}", new Object() {
                @Override
                public String toString() {
                    return UPPERCASE_HEX.printHexBinary(signature.signature);
                }
            });
            end(session, e);
            return true;
        case SIGNER:
            signature.signer = session.read(BytesFormat.INSTANCE);
            LOG.debug("SIGNER : {}", new Object() {
                @Override
                public String toString() {
                    return UPPERCASE_HEX.printHexBinary(signature.signer);
                }
            });
            end(session, e);
            return true;
        // $CASES-OMITTED$
        default:
            return false;
        // throw new TiesDBProtocolException("Illegal packet format");
        }
    }

    @Override
    public boolean accept(Conversation session, Event e, Signature signature) throws TiesDBProtocolException {
        acceptEach(session, e, this::acceptSignature, signature);
        return true;
    }

}
