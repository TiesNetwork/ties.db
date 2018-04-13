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
package com.tiesdb.protocol.v0r0.util;

import java.io.IOException;

import one.utopic.abio.api.output.Output;

public final class DataFormatHelper {

    private DataFormatHelper() {
    }

    /* UTILITY FUNCTIONS */

    public static void writeBytes(Output out, byte[] buf) throws IOException {
        for (int i = 0; i < buf.length; i++) {
            out.writeByte(buf[i]);
        }
    }

    public static void writeLong32(CheckedConsumer<Byte, IOException> out, long value) throws IOException {
        writeLong(out, value, 4);
    }

    public static void writeInt16(CheckedConsumer<Byte, IOException> out, int value) throws IOException {
        writeLong(out, value, 2);
    }

    public static void writeLong(CheckedConsumer<Byte, IOException> out, long value, int bytes) throws IOException {
        for (int i = bytes; i > 0; --i) {
            out.accept((byte) (0xFF & (value >>> (8 * (i - 1)))));
        }
    }

    public static void skip(CheckedSupplier<Byte, IOException> sup, int bytes) throws IOException {
        for (int i = bytes; i > 0; --i) {
            sup.get();
        }
    }

    public static long parseLong64(CheckedSupplier<Byte, IOException> sup) throws IOException {
        return parseLong(sup, 8);
    }

    public static long parseLong32(CheckedSupplier<Byte, IOException> sup) throws IOException {
        return parseLong(sup, 4);
    }

    public static int parseInt32(CheckedSupplier<Byte, IOException> sup) throws IOException {
        return parseInt(sup, 4);
    }

    public static int parseInt16(CheckedSupplier<Byte, IOException> sup) throws IOException {
        return parseInt(sup, 2);
    }

    public static int parseInt(CheckedSupplier<Byte, IOException> sup, int bytes) throws IOException {
        int value = 0;
        for (int i = bytes; i > 0; --i) {
            value |= ((int) sup.get() & 0xff) << (8 * (i - 1));
        }
        return value;
    }

    public static long parseLong(CheckedSupplier<Byte, IOException> sup, int bytes) throws IOException {
        long value = 0;
        for (int i = bytes; i > 0; --i) {
            value |= ((long) sup.get() & 0xff) << (8 * (i - 1));
        }
        return value;
    }
}
