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
package com.tiesdb.protocol.v0r0.test.util;

import static com.tiesdb.protocol.v0r0.util.DataFormatHelper.writeBytes;
import static com.tiesdb.protocol.v0r0.util.DataFormatHelper.writeInt16;
import static com.tiesdb.protocol.v0r0.util.DataFormatHelper.writeLong32;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.tiesdb.protocol.v0r0.ebml.TiesDBType;
import com.tiesdb.protocol.v0r0.ebml.TiesEBMLReader;
import com.tiesdb.protocol.v0r0.ebml.TiesEBMLWriter;
import com.tiesdb.protocol.v0r0.ebml.format.FixedSupplierFormat;
import com.tiesdb.protocol.v0r0.util.CheckedConsumer;
import com.tiesdb.lib.crypto.checksum.ChecksumManager;
import com.tiesdb.lib.crypto.checksum.api.Checksum;
import com.tiesdb.lib.crypto.digest.DigestManager;
import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.lib.crypto.ecc.signature.ECKey;
import com.tiesdb.protocol.api.Version;
import com.tiesdb.protocol.api.TiesDBProtocol.TiesDBChannelInput;
import com.tiesdb.protocol.api.TiesDBProtocol.TiesDBChannelOutput;
import com.tiesdb.protocol.exception.TiesDBException;

import one.utopic.abio.api.output.Output;
import one.utopic.sparse.ebml.EBMLEvent;
import one.utopic.sparse.ebml.EBMLReader;
import one.utopic.sparse.ebml.EBMLWriter;
import one.utopic.sparse.ebml.EBMLType.Context;
import one.utopic.sparse.ebml.EBMLWriter.EBMLWriteFormat;
import one.utopic.sparse.ebml.format.BytesFormat;

public final class TestUtil {

    private static final byte[] PACKET_HEADER_MAGIC_NUMBER = new byte[] { (byte) 0xc0, 0x01, (byte) 0xba, 0x5e };
    private static final int PACKET_HEADER_RESERVED_LEN = 2;

    public static final String DEFAULT_DIGEST_NAME = DigestManager.KECCAK_256;
    public static final FixedSupplierFormat<byte[]> DELEGATE_HASH_FORMAT = new FixedSupplierFormat<>(BytesFormat.INSTANCE, 32);
    public static final FixedSupplierFormat<byte[]> DELEGATE_SIGN_FORMAT = new FixedSupplierFormat<>(BytesFormat.INSTANCE, 32 + 32 + 1);

    public static byte[] getPacketHeader(Version v) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            writePacketHeader(v, createOutput(baos));
            return baos.toByteArray();
        }
    }

    public static void writePacketHeader(Version v, Output out) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Checksum checksum = ChecksumManager.getChecksum(ChecksumManager.CRC32);
        CheckedConsumer<Byte, IOException> con = (b) -> {
            checksum.update(b);
            baos.write(b);
        };
        for (int i = 0; i < PACKET_HEADER_RESERVED_LEN; i++) {
            con.accept((byte) 0);
        }
        writeInt16(con, v.getVersion());
        writeInt16(con, v.getRevision());
        writeInt16(con, v.getMaintence());

        writeBytes(out, PACKET_HEADER_MAGIC_NUMBER);
        writeLong32((b) -> out.writeByte(b), checksum.getValue());
        writeBytes(out, baos.toByteArray());
    }

    public static <W extends EBMLWriter> Consumer<W> write(byte[]... data) {
        return write(BytesFormat.INSTANCE, data);
    }

    @SafeVarargs
    public static <W extends EBMLWriter, O> Consumer<W> write(EBMLWriteFormat<O> format, O... data) {
        return w -> {
            for (O o : data) {
                format.write(w, o);
            }
        };
    }

    public static <W extends EBMLWriter, O> Consumer<W> part(TiesDBType type, EBMLWriteFormat<O> format, O data) {
        return w -> {
            w.accept(new EBMLEvent(type, EBMLEvent.CommonEventType.BEGIN));
            write(format, data).accept(w);
            w.accept(new EBMLEvent(type, EBMLEvent.CommonEventType.END));
        };
    }

    @SafeVarargs
    public static <W extends EBMLWriter> Consumer<W> part(TiesDBType type, Consumer<? super W>... consumers) {
        return w -> {
            w.accept(new EBMLEvent(type, EBMLEvent.CommonEventType.BEGIN));
            for (Consumer<? super W> consumer : consumers) {
                consumer.accept(w);
            }
            w.accept(new EBMLEvent(type, EBMLEvent.CommonEventType.END));
        };
    }

    @SafeVarargs
    public static <W extends TiesEBMLWriter> Consumer<W> tiesPartSign(ECKey key, TiesDBType type, Consumer<? super W>... consumers) {
        Digest digest = DigestManager.getDigest(DEFAULT_DIGEST_NAME);
        return tiesPart(type, digest::update, DELEGATE_SIGN_FORMAT, () -> {
            byte[] out = new byte[digest.getDigestSize()];
            digest.doFinal(out, 0);
            byte[] sig = key.sign(out).toByteArray();
            return sig;
        }, consumers);
    }

    @SafeVarargs
    public static <W extends TiesEBMLWriter> Consumer<W> tiesPartHash(TiesDBType type, Consumer<? super W>... consumers) {
        Digest digest = DigestManager.getDigest(DEFAULT_DIGEST_NAME);
        return tiesPart(type, digest::update, DELEGATE_HASH_FORMAT, () -> {
            byte[] out = new byte[digest.getDigestSize()];
            digest.doFinal(out, 0);
            return out;
        }, consumers);
    }

    @SafeVarargs
    public static <W extends TiesEBMLWriter, O> Consumer<W> tiesPart(TiesDBType type, Consumer<Byte> c, EBMLWriteFormat<Supplier<O>> f,
            Supplier<O> s, Consumer<? super W>... consumers) {
        return w -> {
            ties(c, consumers).accept(w);
            w.accept(new EBMLEvent(type, EBMLEvent.CommonEventType.BEGIN));
            f.write(w, s);
            w.accept(new EBMLEvent(type, EBMLEvent.CommonEventType.END));
        };
    }

    @SafeVarargs
    public static <W extends TiesEBMLWriter, O> Consumer<W> ties(Consumer<Byte> c, Consumer<? super W>... consumers) {
        return w -> {
            w.addListener(c);
            for (Consumer<? super W> consumer : consumers) {
                consumer.accept(w);
            }
            w.removeListener(c);
        };
    }

    @SafeVarargs
    public static byte[] encodeTies(Consumer<? super TiesEBMLWriter>... consumers) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            for (Consumer<? super TiesEBMLWriter> consumer : consumers) {
                consumer.accept(new TiesEBMLWriter(createOutput(baos)));
            }
            return baos.toByteArray();
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    @SafeVarargs
    public static byte[] encode(Consumer<? super EBMLWriter>... consumers) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            for (Consumer<? super EBMLWriter> consumer : consumers) {
                consumer.accept(new EBMLWriter(createOutput(baos)));
            }
            return baos.toByteArray();
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    public static byte[] channel(byte[] data, CheckedBiConsumer<TiesDBChannelInput, TiesDBChannelOutput, TiesDBException> c) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            c.accept(createInput(bais), createOutput(baos));
            return baos.toByteArray();
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    private static StreamOutput createOutput(ByteArrayOutputStream baos) {
        return new StreamOutput(baos);
    }

    public static void decode(byte[] data, Context context, Consumer<EBMLReader> r) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            r.accept(new EBMLReader(createInput(bais), context));
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    public static void decodeTies(byte[] data, Context context, Consumer<TiesEBMLReader> r) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            r.accept(new TiesEBMLReader(createInput(bais), context));
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    private static StreamInput createInput(ByteArrayInputStream bais) {
        return new StreamInput(bais);
    }

    private TestUtil() {
    }

    @FunctionalInterface
    public static interface CheckedBiConsumer<T, U, E extends Throwable> {

        void accept(T t, U u) throws E;

        default CheckedBiConsumer<T, U, E> andThen(CheckedBiConsumer<? super T, ? super U, ? extends E> after) throws E {
            Objects.requireNonNull(after);

            return (l, r) -> {
                accept(l, r);
                after.accept(l, r);
            };
        }

        default CheckedBiConsumer<T, U, E> andThen(BiConsumer<? super T, ? super U> after) throws E {
            Objects.requireNonNull(after);

            return (l, r) -> {
                accept(l, r);
                after.accept(l, r);
            };
        }
    }
}
