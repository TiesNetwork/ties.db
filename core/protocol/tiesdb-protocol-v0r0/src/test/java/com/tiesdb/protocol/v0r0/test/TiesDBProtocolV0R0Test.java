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
package com.tiesdb.protocol.v0r0.test;

import static com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.VERSION;
import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.*;
import static com.tiesdb.protocol.v0r0.test.util.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.tiesdb.lib.crypto.digest.DigestManager;
import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.lib.crypto.ecc.signature.ECKey;
import com.tiesdb.protocol.TiesDBProtocolManager;
import com.tiesdb.protocol.api.TiesDBProtocol;
import com.tiesdb.protocol.api.TiesDBProtocolHandler;
import com.tiesdb.protocol.api.TiesDBProtocolHandlerProvider;
import com.tiesdb.protocol.api.Version;
import com.tiesdb.protocol.exception.TiesDBException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.ebml.TiesDBType;
import com.tiesdb.protocol.v0r0.ebml.TiesDBType.Context;
import com.tiesdb.protocol.v0r0.ebml.TiesEBMLType;
import com.tiesdb.protocol.v0r0.test.util.TestUtil;
import com.tiesdb.protocol.v0r0.test.util.TestUtil.CheckedBiConsumer;

import one.utopic.sparse.ebml.EBMLEvent;
import one.utopic.sparse.ebml.EBMLType;
import one.utopic.sparse.ebml.format.BytesFormat;
import one.utopic.sparse.ebml.format.IntegerFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

public class TiesDBProtocolV0R0Test {

    protected static Version[] getProtocolHanshakeTestVersions() {
        return new Version[] { //
                VERSION, //
                new Version(VERSION.getVersion(), VERSION.getRevision(), 0), //
                new Version(VERSION.getVersion(), VERSION.getRevision(), Short.MAX_VALUE), //
        };
    }

    private void protocolHanshakeTest(TiesDBProtocol p, Version clientVersion,
            CheckedBiConsumer<TiesDBProtocol.TiesDBChannelInput, TiesDBProtocol.TiesDBChannelOutput, TiesDBException> c)
            throws IOException {
        Version protocolVersion = p.getVersion();
        assertNotNull(p);

        byte[] protocolVersionHeader = TestUtil.getPacketHeader(protocolVersion);
        byte[] packetHeader = TestUtil.getPacketHeader(clientVersion);

        byte[] encDataRequest = channel(packetHeader, c);
        if (Boolean.getBoolean("test-verbose")) {
            System.out.print(javax.xml.bind.DatatypeConverter.printHexBinary(packetHeader));
            System.out.print(" == ");
            System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(encDataRequest));
        }
        assertArrayEquals(protocolVersionHeader, encDataRequest);
    }

    @DisplayName("ProtocolHanshakeCreateTest")
    @ParameterizedTest(name = "{index}. {arguments}")
    @MethodSource("getProtocolHanshakeTestVersions")
    @SuppressWarnings("unchecked")
    public void protocolHanshakeCreateTest(Version clientVersion) throws IOException {
        TiesDBProtocol p = TiesDBProtocolManager.getProtocol(VERSION);
        protocolHanshakeTest(p, clientVersion, (in, out) -> p.createChannel(out, new TiesDBProtocolHandlerProvider() {
            @Override
            public <S> TiesDBProtocolHandler<S> getHandler(Version localVersion, Version remoteVersion, S session) {
                assertEquals(VERSION, localVersion);
                assertEquals(clientVersion, remoteVersion);
                return (TiesDBProtocolHandler<S>) new TiesDBProtocolHandler<Conversation>() {
                    @Override
                    public void handle(Conversation c) throws TiesDBException {
                    }
                };
            }
        }));
    }

    @DisplayName("ProtocolHanshakeAcceptTest")
    @ParameterizedTest(name = "{index}. {arguments}")
    @MethodSource("getProtocolHanshakeTestVersions")
    @SuppressWarnings("unchecked")
    public void protocolHanshakeAcceptTest(Version clientVersion) throws IOException {
        TiesDBProtocol p = TiesDBProtocolManager.getProtocol(VERSION);
        protocolHanshakeTest(p, clientVersion, (in, out) -> p.acceptChannel(in, out, new TiesDBProtocolHandlerProvider() {
            @Override
            public <S> TiesDBProtocolHandler<S> getHandler(Version localVersion, Version remoteVersion, S session) {
                assertEquals(VERSION, localVersion);
                assertEquals(clientVersion, remoteVersion);
                return (TiesDBProtocolHandler<S>) new TiesDBProtocolHandler<Conversation>() {
                    @Override
                    public void handle(Conversation c) throws TiesDBException {
                    }
                };
            }
        }));
    }

    @Test
    public void signatureTest() {
        ECKey key = ECKey.fromPrivate(getRandomBa(32));

        byte[] encData = encodeTies(part(ENTRY_HEADER, //
                tiesPartSign(key, SIGNATURE, //
                        part(TABLESPACE_NAME, UTF8StringFormat.INSTANCE, "filatov-dev.node.dev.tablespace"), //
                        part(TABLE_NAME, UTF8StringFormat.INSTANCE, "dev-test"), //
                        // part(ENTRY_TYPE, IntegerFormat.INSTANCE, 0x01), //
                        part(ENTRY_VERSION, IntegerFormat.INSTANCE, 0x01), //
                        part(ENTRY_NETWORK, IntegerFormat.INSTANCE, 60), //
                        part(SIGNER, BytesFormat.INSTANCE, key.getAddress()) //
                ) //
        ));
        decodeTies(encData, ENTRY.getContext(), r -> {
            Digest headerHash = DigestManager.getDigest(DEFAULT_DIGEST_NAME);
            Consumer<Byte> headerHashListener = headerHash::update;
            byte[] sigRef = null;
            byte[] signerRef = null;
            while (r.hasNext()) {
                EBMLEvent event = r.next();
                if (EBMLEvent.CommonEventType.BEGIN.equals(event.getType())) {
                    switch ((TiesDBType) event.get()) {
                    case ENTRY_HEADER:
                        r.addListener(headerHashListener);
                        break;
                    case SIGNER:
                        assertNull(signerRef);
                        signerRef = BytesFormat.INSTANCE.read(r);
                        break;
                    case SIGNATURE:
                        r.removeListener(headerHashListener);
                        assertNull(sigRef);
                        sigRef = BytesFormat.INSTANCE.read(r);
                        r.addListener(headerHashListener);
                        break;
                    // $CASES-OMITTED$
                    default:
                        if (event.get() instanceof TiesEBMLType && !((TiesEBMLType) event.get()).isStructural()) {
                            BytesFormat.INSTANCE.read(r);
                        }
                    }
                }
                if (EBMLEvent.CommonEventType.END.equals(event.getType())) {
                    switch ((TiesDBType) event.get()) {
                    case ENTRY_HEADER:
                        r.removeListener(headerHashListener);
                        break;
                    // $CASES-OMITTED$
                    default:
                    }
                }
            }
            byte[] hash = getBytes(headerHash);
            byte[] sig = key.sign(hash).toByteArray();
            assertArrayEquals(sig, sigRef);
            try {
                byte[] signer = ECKey.signatureToAddressBytes(hash, sigRef);
                assertArrayEquals(signer, signerRef);
            } catch (SignatureException e) {
                fail(e);
            }
        });
    }

    private byte[] getBytes(Digest digest) {
        byte[] out = new byte[digest.getDigestSize()];
        digest.doFinal(out, 0);
        return out;
    }

    private byte[] getRandomBa(int size) {
        byte[] buf = new byte[size];
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.nextBytes(buf);
        } catch (NoSuchAlgorithmException e) {
            fail(e);
        }
        return buf;
    }

    @Test // TODO rewrite this masterpiece of stupidity
    public void bigPacketWriteReadTest() {
        byte[][] stubDataArray = new byte[][] { {}, { 0x0D, 0x0D, 0x0D } };
        for (byte[] stubData : stubDataArray) {
            byte[] encData = encode(//
                    part(MODIFICATION_REQUEST, //
                            part(CONSISTENCY, BytesFormat.INSTANCE, stubData), //
                            part(ENTRY, //
                                    part(ENTRY_HEADER, //
                                            part(TABLESPACE_NAME, BytesFormat.INSTANCE, stubData), //
                                            part(TABLE_NAME, BytesFormat.INSTANCE, stubData), //
                                            // part(ENTRY_TYPE, BytesFormat.INSTANCE, stubData), //
                                            part(ENTRY_TIMESTAMP, BytesFormat.INSTANCE, stubData), //
                                            part(ENTRY_VERSION, BytesFormat.INSTANCE, stubData), //
                                            part(ENTRY_OLD_HASH, BytesFormat.INSTANCE, stubData), //
                                            part(ENTRY_FLD_HASH, BytesFormat.INSTANCE, stubData), //
                                            part(ENTRY_NETWORK, BytesFormat.INSTANCE, stubData), //
                                            part(SIGNATURE, BytesFormat.INSTANCE, stubData) //
                                    ), part(FIELD_LIST, //
                                            part(FIELD, //
                                                    part(FIELD_NAME, BytesFormat.INSTANCE, stubData), //
                                                    part(FIELD_TYPE, BytesFormat.INSTANCE, stubData), //
                                                    part(FIELD_HASH, BytesFormat.INSTANCE, stubData) //
                                            ), //
                                            part(FIELD, //
                                                    part(FIELD_NAME, BytesFormat.INSTANCE, stubData), //
                                                    part(FIELD_TYPE, BytesFormat.INSTANCE, stubData), //
                                                    part(FIELD_VALUE, BytesFormat.INSTANCE, stubData) //
                                            ) //
                                    ), //
                                    part(CHEQUE_LIST, //
                                            part(CHEQUE, //
                                                    part(CHEQUE_RANGE, BytesFormat.INSTANCE, stubData), //
                                                    part(CHEQUE_NUMBER, BytesFormat.INSTANCE, stubData), //
                                                    part(CHEQUE_TIMESTAMP, BytesFormat.INSTANCE, stubData), //
                                                    part(CHEQUE_AMOUNT, BytesFormat.INSTANCE, stubData), //
                                                    part(ADDRESS_LIST, //
                                                            part(ADDRESS, BytesFormat.INSTANCE, stubData), //
                                                            part(ADDRESS, BytesFormat.INSTANCE, stubData), //
                                                            part(ADDRESS, BytesFormat.INSTANCE, stubData) //
                                                    ) //
                                            ), //
                                            part(CHEQUE, //
                                                    part(CHEQUE_RANGE, BytesFormat.INSTANCE, stubData), //
                                                    part(CHEQUE_NUMBER, BytesFormat.INSTANCE, stubData), //
                                                    part(CHEQUE_TIMESTAMP, BytesFormat.INSTANCE, stubData), //
                                                    part(CHEQUE_AMOUNT, BytesFormat.INSTANCE, stubData), //
                                                    part(ADDRESS_LIST, //
                                                            part(ADDRESS, BytesFormat.INSTANCE, stubData), //
                                                            part(ADDRESS, BytesFormat.INSTANCE, stubData), //
                                                            part(ADDRESS, BytesFormat.INSTANCE, stubData) //
                                                    ), //
                                                    part(SIGNATURE, BytesFormat.INSTANCE, stubData) //
                                            ) //
                                    ) //
                            ) //
                    ) //
            );
            if (Boolean.getBoolean("test-verbose")) {
                System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(encData));
            }
            decode(encData, Context.ROOT, reader -> {
                LinkedList<EBMLType> stack = new LinkedList<>();
                reader.forEachRemaining(event -> {
                    if (EBMLEvent.CommonEventType.BEGIN.equals(event.getType())) {
                        stack.push(event.get());
                        if (Context.VALUE.equals(event.get().getContext())) {
                            assertArrayEquals(stubData, BytesFormat.INSTANCE.read(reader));
                            switch ((TiesDBType) event.get()) {
                            case ENTRY_NETWORK:
                                assertArrayEquals(new EBMLType[] { ENTRY_NETWORK, ENTRY_HEADER, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case ADDRESS:
                                assertArrayEquals(
                                        new EBMLType[] { ADDRESS, ADDRESS_LIST, CHEQUE, CHEQUE_LIST, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case CHEQUE_AMOUNT:
                                assertArrayEquals(new EBMLType[] { CHEQUE_AMOUNT, CHEQUE, CHEQUE_LIST, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case CHEQUE_NUMBER:
                                assertArrayEquals(new EBMLType[] { CHEQUE_NUMBER, CHEQUE, CHEQUE_LIST, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case CHEQUE_RANGE:
                                assertArrayEquals(new EBMLType[] { CHEQUE_RANGE, CHEQUE, CHEQUE_LIST, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case CHEQUE_TIMESTAMP:
                                assertArrayEquals(new EBMLType[] { CHEQUE_TIMESTAMP, CHEQUE, CHEQUE_LIST, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case CONSISTENCY:
                                assertArrayEquals(new EBMLType[] { CONSISTENCY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case ENTRY_FLD_HASH:
                                assertArrayEquals(new EBMLType[] { ENTRY_FLD_HASH, ENTRY_HEADER, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case ENTRY_OLD_HASH:
                                assertArrayEquals(new EBMLType[] { ENTRY_OLD_HASH, ENTRY_HEADER, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case TABLESPACE_NAME:
                                assertArrayEquals(new EBMLType[] { TABLESPACE_NAME, ENTRY_HEADER, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case TABLE_NAME:
                                assertArrayEquals(new EBMLType[] { TABLE_NAME, ENTRY_HEADER, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case ENTRY_TIMESTAMP:
                                assertArrayEquals(new EBMLType[] { ENTRY_TIMESTAMP, ENTRY_HEADER, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            // case ENTRY_TYPE:
                            // assertArrayEquals(new EBMLType[] { ENTRY_TYPE, ENTRY_HEADER,
                            // MODIFICATION_ENTRY, MODIFICATION_REQUEST }, //
                            // stack.toArray());
                            // break;
                            case ENTRY_VERSION:
                                assertArrayEquals(new EBMLType[] { ENTRY_VERSION, ENTRY_HEADER, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case FIELD_HASH:
                                assertArrayEquals(new EBMLType[] { FIELD_HASH, FIELD, FIELD_LIST, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case FIELD_NAME:
                                assertArrayEquals(new EBMLType[] { FIELD_NAME, FIELD, FIELD_LIST, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case FIELD_TYPE:
                                assertArrayEquals(new EBMLType[] { FIELD_TYPE, FIELD, FIELD_LIST, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case FIELD_VALUE:
                                assertArrayEquals(new EBMLType[] { FIELD_VALUE, FIELD, FIELD_LIST, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray());
                                break;
                            case SIGNATURE:
                                if (!(//
                                Arrays.equals(new EBMLType[] { SIGNATURE, CHEQUE, CHEQUE_LIST, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray()) || //
                                Arrays.equals(new EBMLType[] { SIGNATURE, ENTRY_HEADER, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray())//
                                )) {
                                    fail(SIGNATURE + " element missplaced");
                                }
                                break;
                            case SIGNER:
                                if (!(//
                                Arrays.equals(new EBMLType[] { SIGNER, CHEQUE, CHEQUE_LIST, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray()) || //
                                Arrays.equals(new EBMLType[] { SIGNER, ENTRY_HEADER, ENTRY, MODIFICATION_REQUEST }, //
                                        stack.toArray())//
                                )) {
                                    fail(SIGNER + " element missplaced");
                                }
                                break;

                            // STRUCTURAL
                            case ADDRESS_LIST:
                            case CHEQUE:
                            case CHEQUE_LIST:
                            case ENTRY_HEADER:
                            case FIELD:
                            case FIELD_LIST:
                            case MODIFICATION_REQUEST:
                                fail(event.get() + " should be a structure element");
                                break;

                            // NOT READY FOR TEST
                            case ARG_STATIC_TYPE:
                            case ARG_STATIC_VALUE:
                            case COMPUTE_FIELD:
                            case ENTRY_HASH:
                            case ERROR:
                            case ERROR_MESSAGE:
                            case FILTER:
                            case FILTER_FIELD:
                            case FILTER_LIST:
                            case FUNCTION_NAME:
                            case FUN_ARGUMENT_FUNCTION:
                            case FUN_ARGUMENT_REFERENCE:
                            case FUN_ARGUMENT_STATIC:
                            case MESSAGE_ID:
                            case MODIFICATION_ERROR:
                            case MODIFICATION_RESPONSE:
                            case MODIFICATION_RESULT:
                            case RECOLLECTION_COMPUTE:
                            case ENTRY:
                            case RECOLLECTION_REQUEST:
                            case RECOLLECTION_RESPONSE:
                            case RECOLLECTION_RESULT:
                            case RETRIEVE_LIST:
                            case RET_COMPUTE:
                            case RET_COMPUTE_ALIAS:
                            case RET_COMPUTE_TYPE:
                            case RET_FIELD:
                            case UNKNOWN_STRUCTURE:
                            case UNKNOWN_VALUE:
                                fail(event.get() + " not yet ready for test");
                                break;

                            // UNKNOWN
                            default:
                                fail(event.get() + " unknown type");
                                break;
                            }
                        }
                    } else if (EBMLEvent.CommonEventType.END.equals(event.getType())) {
                        stack.pop();
                    }
                });
            });
        }
    }

}
