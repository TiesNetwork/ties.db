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

import java.util.HashMap;
import java.util.Map;

public class Hex {

    public static final String HEX_PREFIX = "0x";

    public static final String HEX_UPPERCASE_CODE = "0123456789ABCDEF";
    public static final String HEX_LOWERCASE_CODE = "0123456789abcdef";

    public static final Hex UPPERCASE_HEX = new Hex(HEX_UPPERCASE_CODE.toCharArray());
    public static final Hex LOWERCASE_HEX = new Hex(HEX_LOWERCASE_CODE.toCharArray());

    private final char[] hexCode;
    private final Map<Character, Integer> hexMap;
    private final String prefix;

    public Hex(char[] hexCode) {
        this(hexCode, null);
    }

    public Hex(char[] hexCode, String prefix) {
        this.hexCode = checkHexCode(hexCode);
        this.hexMap = buildHexMap(this.hexCode);
        this.prefix = null != prefix ? prefix : "";
    }

    private static Map<Character, Integer> buildHexMap(char[] hexCode) {
        Map<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < hexCode.length; i++) {
            map.put(hexCode[i], i);
        }
        return map;
    }

    /**
     * @param hex The hex string to be decoded
     * 
     * @return An array of bytes represented by the <b>hex</b> argument
     * 
     * @throws IllegalArgumentException If string contains illegal characters
     */
    public byte[] parseHexBinary(String hex) {
        return parseHexBinary(hex, this.hexMap);
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
        return printHexBinary(bytes, offset, length, hexCode, prefix);
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
        return formatHexBinary(bytes, offset, length, out, hexCode, prefix);
    }

    private static String printHexBinary(byte[] bytes, int offset, int length, char[] hexCode, String prefix) {
        StringBuilder builder = new StringBuilder();
        formatHexBinary(bytes, offset, length, builder, hexCode, prefix);
        return builder.toString();
    }

    private static int formatHexBinary(byte[] bytes, int offset, int length, StringBuilder out, char[] hexCode, String prefix) {

        checkBounds(bytes, offset, length);

        length = min(offset + length, bytes.length) - offset;

        if (length == 0)
            return 0;

        out.ensureCapacity(prefix.length() + out.length() + length * 2);

        out.append(prefix);

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

    private static char[] checkHexCode(char[] hexCode) {
        if (hexCode.length < 16)
            throw new IllegalArgumentException("Hex code length should be 16. Actual hex code length = " + hexCode.length);
        return hexCode;
    }

    private static byte[] parseHexBinary(String s, Map<Character, Integer> hexMap) {
        final int len = s.length();

        if (len % 2 != 0) {
            throw new IllegalArgumentException("hexBinary needs to be even-length: " + s);
        }

        byte[] out = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            int h = hexMap.getOrDefault(s.charAt(i), -1);
            int l = hexMap.getOrDefault(s.charAt(i + 1), -1);
            if (h == -1 || l == -1) {
                throw new IllegalArgumentException(
                        "contains illegal character in '" + s.charAt(i) + s.charAt(i + 1) + "' at position " + i + " for hexBinary: " + s);
            }

            out[i / 2] = (byte) (h * 16 + l);
        }

        return out;
    }

}
