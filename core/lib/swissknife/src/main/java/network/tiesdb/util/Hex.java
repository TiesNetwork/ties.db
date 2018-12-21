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
package network.tiesdb.util;

public class Hex {

    public static final String HEX_PREFIX = "0x";

    public static final Hex UPPERCASE = new Hex("0123456789ABCDEF".toCharArray());
    public static final Hex LOWERCASE = new Hex("0123456789abcdef".toCharArray());

    private final char[] hexCode;

    public Hex(char[] hexCode) {
        this.hexCode = hexCode;
    }

    /**
     * @param bytes The bytes to be encoded
     * 
     * @return Hex string
     * 
     * @throws IndexOutOfBoundsException If the {@code offset} and the
     *         {@code length} arguments index characters outside the bounds of the
     *         {@code bytes} array
     */
    public String printHexBinary(byte[] bytes) {
        return printHexBinary(bytes, 0, bytes.length);
    }

    /**
     * @param bytes The bytes to be encoded
     *
     * @param offset The index of the first byte to encode
     *
     * @param length The number of bytes to encoder
     * 
     * @return Hex string
     * 
     * @throws IndexOutOfBoundsException If the {@code offset} and the
     *         {@code length} arguments index characters outside the bounds of the
     *         {@code bytes} array
     */
    public String printHexBinary(byte[] bytes, int offset, int length) {
        return printHexBinary(bytes, offset, length, hexCode);
    }

    /**
     * @param bytes The bytes to be encoded
     *
     * @param offset The index of the first byte to encode
     *
     * @param length The number of bytes to encoder
     * 
     * @param out StringBuilder output
     * 
     * @return Read bytes count
     * 
     * @throws IndexOutOfBoundsException If the {@code offset} and the
     *         {@code length} arguments index characters outside the bounds of the
     *         {@code bytes} array
     */
    public int formatHexBinary(byte[] bytes, StringBuilder out) {
        return formatHexBinary(bytes, 0, bytes.length, out);
    }

    /**
     * @param bytes The bytes to be encoded
     *
     * @param offset The index of the first byte to encode
     *
     * @param length The number of bytes to encoder
     * 
     * @param out StringBuilder output
     * 
     * @return Read bytes count
     * 
     * @throws IndexOutOfBoundsException If the {@code offset} and the
     *         {@code length} arguments index characters outside the bounds of the
     *         {@code bytes} array
     */
    public int formatHexBinary(byte[] bytes, int offset, int length, StringBuilder out) {
        return formatHexBinary(bytes, offset, length, out, hexCode);
    }

    private static String printHexBinary(byte[] bytes, int offset, int length, char[] hexCode) {
        StringBuilder builder = new StringBuilder();
        formatHexBinary(bytes, offset, length, builder, hexCode);
        return builder.toString();
    }

    private static int formatHexBinary(byte[] bytes, int offset, int length, StringBuilder out, char[] hexCode) {

        checkBounds(bytes, offset, length);

        length = min(offset + length, bytes.length) - offset;

        if (length == 0)
            return 0;

        out.ensureCapacity(out.length() + length * 2);

        for (int i = offset, end = offset + length; i < end; i++) {
            byte b = bytes[i];
            out.append(hexCode[(b >> 4) & 0xF]);
            out.append(hexCode[(b & 0xF)]);
        }

        return length;

    }

    private static int min(int a, int b) {
        return a > b ? b : a;
    }

    private static void checkBounds(byte[] bytes, int offset, int length) {
        if (length < 0)
            throw new IndexOutOfBoundsException("Array negative length = " + length);
        if (offset < 0)
            throw new IndexOutOfBoundsException("Array negative offset = " + offset);
        if (offset > bytes.length - length)
            throw new IndexOutOfBoundsException("Array is too small for offset + length = " + offset + length);
    }
}
