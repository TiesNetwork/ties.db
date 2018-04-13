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

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.*;
import static com.tiesdb.protocol.v0r0.test.util.TestUtil.*;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.xml.bind.DatatypeConverter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.tiesdb.lib.crypto.digest.DigestManager;
import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.lib.crypto.ecc.signature.ECKey;
import com.tiesdb.protocol.v0r0.ebml.format.UUIDFormat;

import one.utopic.sparse.ebml.format.ASCIIStringFormat;
import one.utopic.sparse.ebml.format.BigIntegerFormat;
import one.utopic.sparse.ebml.format.BytesFormat;
import one.utopic.sparse.ebml.format.DateFormat;
import one.utopic.sparse.ebml.format.IntegerFormat;
import one.utopic.sparse.ebml.format.LongFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

@EnabledIfSystemProperty(named = "test-generate", matches = "true")
public class TiesDBProtocolV0R0Generate {

    @Test
    public void generateSampleDate() {
        Date date = new Date(1522661357000L);
        System.out.println(date);
        byte[] entryDate = encode(//
                part(ENTRY_TIMESTAMP, DateFormat.INSTANCE, date) //
        );
        System.out.println("Date " + DatatypeConverter.printHexBinary(entryDate));
        decode(entryDate, ENTRY_HEADER.getContext(), r -> {
            r.forEachRemaining(e -> {
                System.out.println(DateFormat.INSTANCE.read(r));
            });
        });
    }

    @DisplayName("generateSampleBigNumberTest")
    @ParameterizedTest(name = "{index}. {arguments}")
    @ValueSource(strings = { //
            "0", //
            "-1", //
            "57896044618658097711785492504343953926634992332820282019728792003956564819967", //
            "-57896044618658097711785492504343953926634992332820282019728792003956564819968", //
    })
    public void generateSampleBigNumber(String value) {
        BigInteger amount = new BigInteger(value);
        byte[] entryDate = encode(//
                part(CHEQUE_AMOUNT, BigIntegerFormat.INSTANCE, amount) //
        );
        System.out.println("BigNumber  DEC " + value + "\nBigNumber EBML " + DatatypeConverter.printHexBinary(entryDate));
    }

    @Test
    public void generateSampleInsertRequest() {
        Date date = new Date(1522661357000L);
        ECKey key = ECKey.fromPrivate(hs2ba("b84f0b9766fb4b7e88f11f124f98170cb437cd09515caf970da886e4ef4c5fa3"));

        LinkedList<Supplier<byte[]>> fieldHashes = new LinkedList<>();

        byte[] fieldsData = encodeTies(//
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "string"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "description"), //
                                part(FIELD_VALUE, UTF8StringFormat.INSTANCE, "Initial description") //
                        ) //
                ), //
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "binary"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "uuid"), //
                                part(FIELD_VALUE, UUIDFormat.INSTANCE, UUID.fromString("75e6b139-de73-4e31-9600-e216e4239030")) //
                        ) //
                ) //
        );

        byte[] fldsHash = getHash(d -> fieldHashes.forEach(s -> {
            byte[] hash = s.get();
            System.out.println("FieldHash: " + DatatypeConverter.printHexBinary(hash));
            d.update(hash);
        }));

        System.out.println("EntryFieldsHash: " + DatatypeConverter.printHexBinary(fldsHash));

        byte[] encData = encodeTies(//
                part(MODIFICATION_REQUEST, //
                        part(CONSISTENCY, IntegerFormat.INSTANCE, 0x64), // ALL
                        part(ENTRY, //
                                part(ENTRY_HEADER, //
                                        tiesPartSign(key, SIGNATURE, //
                                                part(ENTRY_TABLESPACE_NAME, UTF8StringFormat.INSTANCE, "filatov-dev.node.dev.tablespace"), //
                                                part(ENTRY_TABLE_NAME, UTF8StringFormat.INSTANCE, "dev-test"), //
                                                part(ENTRY_TYPE, IntegerFormat.INSTANCE, 0x01), // INSERT
                                                part(ENTRY_TIMESTAMP, DateFormat.INSTANCE, date), //
                                                part(ENTRY_VERSION, IntegerFormat.INSTANCE, 0x01), // creating version 1
                                                part(ENTRY_FLD_HASH, BytesFormat.INSTANCE, fldsHash), //
                                                part(ENTRY_NETWORK, IntegerFormat.INSTANCE, 60), //
                                                part(SIGNER, BytesFormat.INSTANCE, key.getAddress()) //
                                        ) //
                                ), //
                                part(FIELD_LIST, BytesFormat.INSTANCE, fieldsData), //
                                part(CHEQUE_LIST, //
                                        part(CHEQUE, //
                                                tiesPartSign(key, SIGNATURE, //
                                                        part(CHEQUE_RANGE, UUIDFormat.INSTANCE,
                                                                UUID.fromString("38007241-b550-4fa5-87d6-8ee7587d4073")), //
                                                        part(CHEQUE_NUMBER, LongFormat.INSTANCE, 1L), //
                                                        part(CHEQUE_TIMESTAMP, DateFormat.INSTANCE, date), //
                                                        part(CHEQUE_AMOUNT, BigIntegerFormat.INSTANCE, BigInteger.ONE), //
                                                        part(ADDRESS_LIST, //
                                                                part(ADDRESS, BytesFormat.INSTANCE,
                                                                        hs2ba("64ed31c6187765D40271EE4F9b4C29A5a125DE23")) //
                                                        ), //
                                                        part(SIGNER, BytesFormat.INSTANCE, key.getAddress()) //
                                                ) //
                                        ) //
                                ) //
                        ) //
                ) //
        );
        System.out.println("Insert " + DatatypeConverter.printHexBinary(encData));
    }

    @Test
    public void generateSampleUpdateRequest() {
        Date date = new Date(1522661357000L);
        ECKey key = ECKey.fromPrivate(hs2ba("b84f0b9766fb4b7e88f11f124f98170cb437cd09515caf970da886e4ef4c5fa3"));

        LinkedList<Supplier<byte[]>> fieldHashes = new LinkedList<>();

        byte[] uuidHash = getHash(//
                d -> encodeTies(//
                        ties(d::update, //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "uuid"), //
                                part(FIELD_VALUE, UUIDFormat.INSTANCE, UUID.fromString("75e6b139-de73-4e31-9600-e216e4239030")) //
                        ) //
                ) //
        );

        byte[] fieldsData = encodeTies(//
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "string"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "description"), //
                                part(FIELD_VALUE, UTF8StringFormat.INSTANCE, "Updated description") //
                        ) //
                ), //
                /*
                 * part(FIELD, // part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "binary"), //
                 * part(FIELD_NAME, UTF8StringFormat.INSTANCE, "uuid"), //
                 * ties(newBytesConsumer(fieldHashes), // part(FIELD_HASH, BytesFormat.INSTANCE,
                 * uuidHash) // ) // ) //
                 */
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "binary"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "uuid"), //
                                part(FIELD_VALUE, UUIDFormat.INSTANCE, UUID.fromString("75e6b139-de73-4e31-9600-e216e4239030")) //
                        ) //
                ) //
        );

        byte[] fldsHash = getHash(d -> fieldHashes.forEach(s -> {
            byte[] hash = s.get();
            System.out.println("FieldHash: " + DatatypeConverter.printHexBinary(hash));
            d.update(hash);
        }));

        System.out.println("EntryFieldsHash: " + DatatypeConverter.printHexBinary(fldsHash));

        byte[] encData = encodeTies(//
                part(MODIFICATION_REQUEST, //
                        part(CONSISTENCY, IntegerFormat.INSTANCE, 0x64), // ALL
                        part(ENTRY, //
                                part(ENTRY_HEADER, //
                                        tiesPartSign(key, SIGNATURE, //
                                                part(ENTRY_TABLESPACE_NAME, UTF8StringFormat.INSTANCE, "filatov-dev.node.dev.tablespace"), //
                                                part(ENTRY_TABLE_NAME, UTF8StringFormat.INSTANCE, "dev-test"), //
                                                part(ENTRY_TYPE, IntegerFormat.INSTANCE, 0x02), // UPDATE
                                                part(ENTRY_TIMESTAMP, DateFormat.INSTANCE, date), //
                                                part(ENTRY_VERSION, IntegerFormat.INSTANCE, 0x02), // from 1 to 2
                                                part(ENTRY_OLD_HASH, BytesFormat.INSTANCE,
                                                        hs2ba("9c22ff5f21f0b81b113e63f7db6da94fedef11b2119b4088b89664fb9a3cb658")), //
                                                part(ENTRY_FLD_HASH, BytesFormat.INSTANCE, fldsHash), //
                                                part(ENTRY_NETWORK, IntegerFormat.INSTANCE, 60), //
                                                part(SIGNER, BytesFormat.INSTANCE, key.getAddress()) //
                                        ) //
                                ), //
                                part(FIELD_LIST, BytesFormat.INSTANCE, fieldsData), //
                                part(CHEQUE_LIST, //
                                        part(CHEQUE, //
                                                tiesPartSign(key, SIGNATURE, //
                                                        part(CHEQUE_RANGE, UUIDFormat.INSTANCE,
                                                                UUID.fromString("38007241-b550-4fa5-87d6-8ee7587d4073")), //
                                                        part(CHEQUE_NUMBER, LongFormat.INSTANCE, 2L), //
                                                        part(CHEQUE_TIMESTAMP, DateFormat.INSTANCE, date), //
                                                        part(CHEQUE_AMOUNT, BigIntegerFormat.INSTANCE, BigInteger.ONE), //
                                                        part(ADDRESS_LIST, //
                                                                part(ADDRESS, BytesFormat.INSTANCE,
                                                                        hs2ba("64ed31c6187765D40271EE4F9b4C29A5a125DE23")) //
                                                        ), //
                                                        part(SIGNER, BytesFormat.INSTANCE, key.getAddress()) //
                                                ) //
                                        ) //
                                ) //
                        ) //
                ) //
        );
        System.out.println("Update " + DatatypeConverter.printHexBinary(encData));
    }

    private byte[] getHash(Consumer<Digest> d) {
        Digest digest = DigestManager.getDigest(DEFAULT_DIGEST_NAME);
        d.accept(digest);
        byte[] out = new byte[digest.getDigestSize()];
        digest.doFinal(out, 0);
        return out;
    }

    private Consumer<Byte> newDigestConsumer(LinkedList<Supplier<byte[]>> fieldHashes) {
        Digest digest = DigestManager.getDigest(DEFAULT_DIGEST_NAME);
        fieldHashes.addLast(() -> {
            byte[] buf = new byte[digest.getDigestSize()];
            digest.doFinal(buf, 0);
            return buf;
        });
        return digest::update;
    }

    private Consumer<Byte> newBytesConsumer(LinkedList<Supplier<byte[]>> fieldHashes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fieldHashes.addLast(() -> {
            return baos.toByteArray();
        });
        return baos::write;
    }

    private byte[] hs2ba(String hexString) {
        return DatatypeConverter.parseHexBinary(hexString);
    }

}
