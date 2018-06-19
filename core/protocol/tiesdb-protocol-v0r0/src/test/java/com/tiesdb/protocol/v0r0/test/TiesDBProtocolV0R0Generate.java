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
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;
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
import com.tiesdb.protocol.v0r0.ebml.TiesDBType;
import com.tiesdb.protocol.v0r0.ebml.TiesDBType.Context;
import com.tiesdb.protocol.v0r0.ebml.format.UUIDFormat;

import one.utopic.sparse.api.Event.CommonEventType;
import one.utopic.sparse.ebml.EBMLEvent;
import one.utopic.sparse.ebml.EBMLType;
import one.utopic.sparse.ebml.EBMLReader.EBMLReadFormat;
import one.utopic.sparse.ebml.format.ASCIIStringFormat;
import one.utopic.sparse.ebml.format.BigDecimalFormat;
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
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "uuid"), //
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
                        part(MODIFICATION_ENTRY, //
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

        byte[] fieldsData = encodeTies(//
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "string"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "description"), //
                                part(FIELD_VALUE, UTF8StringFormat.INSTANCE, "Updated description") //
                        ) //
                ), //
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "uuid"), //
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
                        part(MODIFICATION_ENTRY, //
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

    @Test
    public void generateMultivalueSampleInsertRequest() {
        Date date = new Date(1522661357000L);
        ECKey key = ECKey.fromPrivate(hs2ba("b84f0b9766fb4b7e88f11f124f98170cb437cd09515caf970da886e4ef4c5fa3"));

        LinkedList<Supplier<byte[]>> fieldHashes = new LinkedList<>();

        Random rg = new SecureRandom();

        byte[] randomBytes = new byte[rg.nextInt(32) + 32];
        rg.nextBytes(randomBytes);
        System.out.println("randomBytes " + DatatypeConverter.printHexBinary(randomBytes));
        byte[] fieldsData = encodeTies(//
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "uuid"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "Id"), //
                                part(FIELD_VALUE, UUIDFormat.INSTANCE, UUID.randomUUID()) //
                        ) //
                ), //
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "binary"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "fBinary"), //
                                part(FIELD_VALUE, BytesFormat.INSTANCE, randomBytes) //
                        ) //
                ), //
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "boolean"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "fBoolean"), //
                                part(FIELD_VALUE, IntegerFormat.INSTANCE, rg.nextInt(2)) //
                        ) //
                ), //
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "decimal"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "fDecimal"), //
                                part(FIELD_VALUE, BigDecimalFormat.INSTANCE, new BigDecimal(rg.nextDouble())) //
                        ) //
                ), //
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "double"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "fDouble"), //
                                part(FIELD_VALUE, BigDecimalFormat.INSTANCE, new BigDecimal(rg.nextDouble())) //
                        ) //
                ), //
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "duration"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "fDuration"), //
                                part(FIELD_VALUE, BigDecimalFormat.INSTANCE, new BigDecimal("267512643.210")) //
                        ) //
                ), //
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "float"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "fFloat"), //
                                part(FIELD_VALUE, BigDecimalFormat.INSTANCE, new BigDecimal(rg.nextDouble())) //
                        ) //
                ), //
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "integer"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "fInteger"), //
                                part(FIELD_VALUE, IntegerFormat.INSTANCE, rg.nextInt()) //
                        ) //
                ), //
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "long"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "fLong"), //
                                part(FIELD_VALUE, LongFormat.INSTANCE, rg.nextLong()) //
                        ) //
                ), //
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "string"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "fString"), //
                                part(FIELD_VALUE, UTF8StringFormat.INSTANCE, this.toString()) //
                        ) //
                ), //
                part(FIELD, //
                        part(FIELD_TYPE, ASCIIStringFormat.INSTANCE, "time"), //
                        ties(newDigestConsumer(fieldHashes), //
                                part(FIELD_NAME, UTF8StringFormat.INSTANCE, "fTime"), //
                                part(FIELD_VALUE, DateFormat.INSTANCE, new Date()) //
                        ) //
                ) //
        );

        byte[] fldsHash = getHash(d -> fieldHashes.forEach(s -> {
            byte[] hash = s.get();
            // System.out.println("FieldHash: " + DatatypeConverter.printHexBinary(hash));
            d.update(hash);
        }));

        // System.out.println("EntryFieldsHash: " +
        // DatatypeConverter.printHexBinary(fldsHash));

        byte[] encData = encodeTies(//
                part(MODIFICATION_REQUEST, //
                        part(CONSISTENCY, IntegerFormat.INSTANCE, 0x64), // ALL
                        part(MESSAGE_ID, LongFormat.INSTANCE, 777L), // ALL
                        part(MODIFICATION_ENTRY, //
                                part(ENTRY_HEADER, //
                                        tiesPartSign(key, SIGNATURE, //
                                                part(ENTRY_TABLESPACE_NAME, UTF8StringFormat.INSTANCE, "client-dev.test"), //
                                                part(ENTRY_TABLE_NAME, UTF8StringFormat.INSTANCE, "all_types"), //
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
        System.out.println("MultivalueInsert " + DatatypeConverter.printHexBinary(encData));
        packetDecode(encData);
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

    private byte[] hs2ba(String hexString) {
        return DatatypeConverter.parseHexBinary(hexString);
    }

    public static void main(String[] args) {
        TreeSet<String> values = new TreeSet<>();
        for (int i = 0; i < args.length; i++) {
            values.add(args[i]);
        }
        values.forEach(System.out::println);
    }

    // @Test
    public void packetDecodeTest() {
        String[] dataStrings = new String[] {
                "1E5449454314EE8164EC820309E1430AE140A7808F636C69656E742D6465762E746573748289616C6C5F747970657384810186857EBE0941C88881018CA03A0065BEC50CD1A4300303DB5396944135D81317058610B471B2AC41DADDE5F08E813CFC94FAFE9C9E7845F446D091C12C74D44C61A0923C00FEC13D4FA923031F342877D0D5193CF97E5213C246E6934E115A28892C23AD0C23D50BD63A6ACE98271680F09B5596330993ECD6AB022A7D21D6B6C1585E40BD02D325D141C7D19C8284757569648082496486905D9A8A68B1494AFE9903901EEF70E82ED1CB828662696E61727980876642696E61727986B8C4F15FF411B49FEF804DD8913451E2092F67FD20F433B186AF71E2B8048176D14B31145A796CCDFCB3FD2A5DDAD5BF10BC30C285BC153134D1968287626F6F6C65616E808866426F6F6C65616E868100D1AC8287646563696D616C808866446563696D616C8697E806824B0E5BAF33E1BC7C24D4A9AADC3ED07E9C5A37B1D1AA8286646F75626C65808766446F75626C658697E8199CB4BDD0B01542C664E6A41E6B118195E2E1C945B3D19D82886475726174696F6E8089664475726174696F6E8686863E48FEFE8AD1A88285666C6F6174808666466C6F61748697E80E5812C7E49B7CCB352DCC9A550E4C8D256F8FF0ECD9D1998287696E7465676572808866496E746567657286848698873CD19782846C6F6E678085664C6F6E6786880E9A521BE2E16886D1D48286737472696E67808766537472696E6786C1636F6D2E7469657364622E70726F746F636F6C2E763072302E746573742E54696573444250726F746F636F6C5630523047656E6572617465403666653761616338D195828474696D6580856654696D65868600801D23E153C14093C14090809038007241B5504FA587D68EE7587D407382810184857EBE0941C8868101A196A09464ED31C6187765D40271EE4F9B4C29A5A125DE23FC94FAFE9C9E7845F446D091C12C74D44C61A0923C00FEC12C06F9333BE122E2F32D8D9D7A9216D27CEE6C4219F72AA56E04D21FA4A0DBC33AB1129244908432DC54A9AEED4E60396178CB27695886CB7D23CA539EBAB2A326",
                "C001BA5E1225EFFF00000000000000011F544945A8EC820309E1A280A0C3ACF777B49DD23E7DEA861D90512B46FFB842F7AE1F53B493B1147A6938CC00",
                "C001BA5E1225EFFF00000000000000011F544945BAEC820309EFB480A0C3ACF777B49DD23E7DEA861D90512B46FFB842F7AE1F53B493B1147A6938CC00E090496E73657274696F6E206661696C6564",
                //
        };

        for (String data : dataStrings) {
            packetDecode(DatatypeConverter.parseHexBinary(data.replaceFirst("C001BA5E1225EFFF0000000000000001", "")));
        }
    }

    @Test
    public void packetDecodeTest2() {
        String[] dataStrings = new String[] {
                "1e54494541f6ee8100ec8101e141ede140a7fc94ae65baf610bad3f0d71aa3c3a8110c2d62cbeb19808f636c69656e742d6465762e746573748289616c6c5f74797065738481018685804c45a6408881018ca0ed58cbba606278f7310d68a6c34cef18d53c2d15277b295b50c72abd7684b2808e813cfec18a4038a539ddd47f152d464b8ecc0c270d3bb0e9d9df9e59b673dd490dfbee0b4a6eb460fa0b3488a30f9f69882a9e905031d310205af113fdd04d9c7ac2afab26d14140d19c8082496482847575696486907feb1c3d769240e59330af4ccf5f992ed19980876642696e617279828662696e6172798686e0a61e5ad74fd195808866426f6f6c65616e8287626f6f6c65616e8680d198808866446563696d616c8287646563696d616c868396fb2dd19e808766446f75626c658286646f75626c65868b416f00db980276caea8182d1a18089664475726174696f6e82886475726174696f6e868a9a00efcee47256c00000d19b808666466c6f61748285666c6f6174868aaefdadfc5d755013dd01d199808866496e74656765728287696e74656765728684193cf1b0d1948085664c6f6e6782846c6f6e67868540d0704ff6d1ad808766537472696e678286737472696e67869a54686973206973205554462d3820d181d182d180d0bed0bad0b0d19480856654696d65828474696d658685804c45a2e8",
                "7FFF4088E09F546965734442457863657074696F6E3A2052657175657374206661696C6564E0E554696573444250726F746F636F6C457863657074696F6E3A20496C6C6567616C206576656E743A2054696573444250726F746F636F6C5630523024436F6E766572736174696F6E244576656E74205B747970653D4649454C442C2073746174653D454E445D",
                //
        };

        for (String data : dataStrings) {
            packetDecode(DatatypeConverter.parseHexBinary(data.toUpperCase().replaceFirst("C001BA5E1225EFFF0000000000000001", "")));
        }
    }

    HashMap<TiesDBType, EBMLReadFormat<?>> formatMap = new HashMap<>();
    {
        formatMap.put(MESSAGE_ID, BigIntegerFormat.INSTANCE);
        formatMap.put(ERROR_MESSAGE, UTF8StringFormat.INSTANCE);
        formatMap.put(ENTRY_TABLESPACE_NAME, UTF8StringFormat.INSTANCE);
        formatMap.put(ENTRY_TABLE_NAME, UTF8StringFormat.INSTANCE);
        formatMap.put(FIELD_NAME, UTF8StringFormat.INSTANCE);
        formatMap.put(FIELD_TYPE, ASCIIStringFormat.INSTANCE);
        formatMap.put(ENTRY_TYPE, IntegerFormat.INSTANCE);
        formatMap.put(ENTRY_VERSION, LongFormat.INSTANCE);
        formatMap.put(ENTRY_NETWORK, IntegerFormat.INSTANCE);
        formatMap.put(ENTRY_TIMESTAMP, DateFormat.INSTANCE);
        formatMap.put(CHEQUE_TIMESTAMP, DateFormat.INSTANCE);
        formatMap.put(CHEQUE_RANGE, UUIDFormat.INSTANCE);
        formatMap.put(CHEQUE_NUMBER, LongFormat.INSTANCE);
        formatMap.put(CHEQUE_AMOUNT, BigIntegerFormat.INSTANCE);

        formatMap.put(RECOLLECTION_TABLESPACE_NAME, UTF8StringFormat.INSTANCE);
        formatMap.put(RECOLLECTION_TABLE_NAME, UTF8StringFormat.INSTANCE);
        formatMap.put(RET_COMPUTE_ALIAS, UTF8StringFormat.INSTANCE);
        formatMap.put(RET_COMPUTE_TYPE, ASCIIStringFormat.INSTANCE);
        formatMap.put(RET_FIELD, UTF8StringFormat.INSTANCE);
        formatMap.put(FUNCTION_NAME, UTF8StringFormat.INSTANCE);
        formatMap.put(FUN_ARGUMENT_REFERENCE, UTF8StringFormat.INSTANCE);
        formatMap.put(ARG_STATIC_TYPE, ASCIIStringFormat.INSTANCE);
        formatMap.put(FILTER_FIELD, UTF8StringFormat.INSTANCE);
    }

    public void packetDecode(byte[] encData) {
        // TiesDBType x = TiesDBType.MODIFICATION_RESPONSE;
        decode(encData, Context.ROOT, r -> {
            int i = 0;
            while (r.hasNext()) {
                EBMLEvent e = r.next();
                EBMLType type = e.get();
                if (e.getType().equals(CommonEventType.BEGIN)) {

                    char[] tab = new char[i];
                    Arrays.fill(tab, '\t');
                    System.out.print(tab);
                    i++;

                    System.out.print("<" + type + " code=\"" + type.getEBMLCode().toHexString() + "\"");
                    if (type.getContext().equals(Context.VALUE)) {
                        EBMLReadFormat<?> format = formatMap.get(type);
                        if (null == format) {
                            System.out.print(" format=\"Hex\">" + DatatypeConverter.printHexBinary(BytesFormat.INSTANCE.read(r)));
                        } else {
                            System.out.print(
                                    " format=\"" + format.getClass().getSimpleName().replaceAll("Format$", "") + "\">" + format.read(r));
                        }
                    } else {
                        System.out.println(">");
                    }
                } else if (e.getType().equals(CommonEventType.END)) {

                    i--;
                    if (!type.getContext().equals(Context.VALUE)) {
                        char[] tab = new char[i];
                        Arrays.fill(tab, '\t');
                        System.out.print(tab);
                    }

                    System.out.println("</" + type + ">");
                } else {
                    fail("Wrong event " + e);
                }
            }
            System.out.println("<!--\n" + DatatypeConverter.printHexBinary(encData) + "\n-->");
            System.out.println();
        });
    }

    @Test
    public void generateSampleSelectRequest() {

        UUID uuid = UUID.fromString("7606fc02-8c19-44ee-99be-a24fc1449008");
        byte[] encData = encodeTies(//
                part(RECOLLECTION_REQUEST, //
                        part(CONSISTENCY, IntegerFormat.INSTANCE, 0x64), // ALL
                        part(MESSAGE_ID, LongFormat.INSTANCE, 777L), // ALL
                        part(RECOLLECTION_TABLESPACE_NAME, UTF8StringFormat.INSTANCE, "client-dev.test"), //
                        part(RECOLLECTION_TABLE_NAME, UTF8StringFormat.INSTANCE, "all_types"), //
                        part(RETRIEVE_LIST, //
                                part(RET_FIELD, UTF8StringFormat.INSTANCE, "Id"), //
                                part(RET_COMPUTE, //
                                        part(RET_COMPUTE_ALIAS, UTF8StringFormat.INSTANCE, "WriteTime"), //
                                        part(RET_COMPUTE_TYPE, UTF8StringFormat.INSTANCE, "binary"), //
                                        part(FUNCTION_NAME, ASCIIStringFormat.INSTANCE, "bigIntAsBlob"), //
                                        part(FUN_ARGUMENT_FUNCTION, //
                                                part(FUNCTION_NAME, ASCIIStringFormat.INSTANCE, "toUnixTimestamp"), //
                                                part(FUN_ARGUMENT_FUNCTION, //
                                                        part(FUNCTION_NAME, ASCIIStringFormat.INSTANCE, "cast"), //
                                                        part(FUN_ARGUMENT_FUNCTION, //
                                                                part(FUNCTION_NAME, ASCIIStringFormat.INSTANCE, "writeTime"), //
                                                                part(FUN_ARGUMENT_REFERENCE, UTF8StringFormat.INSTANCE, "fTime")//
                                                        ), //
                                                        part(FUN_ARGUMENT_STATIC, //
                                                                part(ARG_STATIC_TYPE, ASCIIStringFormat.INSTANCE, "string"), //
                                                                part(ARG_STATIC_VALUE, ASCIIStringFormat.INSTANCE, "date")) //
                                                )//
                                        )//
                                ), //
                                part(RET_COMPUTE, //
                                        part(RET_COMPUTE_ALIAS, UTF8StringFormat.INSTANCE, "TestValue"), //
                                        part(RET_COMPUTE_TYPE, UTF8StringFormat.INSTANCE, "binary"), //
                                        part(FUNCTION_NAME, ASCIIStringFormat.INSTANCE, "intAsBlob"), //
                                        part(FUN_ARGUMENT_STATIC, //
                                                part(ARG_STATIC_TYPE, ASCIIStringFormat.INSTANCE, "integer"), //
                                                part(ARG_STATIC_VALUE, IntegerFormat.INSTANCE, 777))//
                                )//
                        ), part(FILTER_LIST, //
                                part(FILTER, //
                                        part(FILTER_FIELD, UTF8StringFormat.INSTANCE, "Id"), //
                                        part(FUNCTION_NAME, ASCIIStringFormat.INSTANCE, "IN"), //
                                        part(FUN_ARGUMENT_STATIC, //
                                                part(ARG_STATIC_TYPE, ASCIIStringFormat.INSTANCE, "uuid"), //
                                                part(ARG_STATIC_VALUE, UUIDFormat.INSTANCE, uuid)//
                                        )//
                                )//
                        )//
                )//
        );

        packetDecode(encData);
    }

}
