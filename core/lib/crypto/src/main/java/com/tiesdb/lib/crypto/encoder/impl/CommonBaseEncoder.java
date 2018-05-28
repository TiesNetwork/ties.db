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
package com.tiesdb.lib.crypto.encoder.impl;

import com.tiesdb.lib.crypto.encoder.api.CodeWriter;
import com.tiesdb.lib.crypto.encoder.api.Encoder;

public class CommonBaseEncoder implements Encoder {

    public static enum Padding {
        NORMAL, PACKED, NO_PADDING
    }

    private static final int BYTE_SIZE = Byte.SIZE;
    private static final int BYTE_MASK = -1 >>> Integer.SIZE - BYTE_SIZE;

    protected final Padding paddingStrategy;
    protected final byte singlePadding;
    protected final byte doublePadding;

    protected final byte[] encodingTable;
    protected final int codingSize;
    protected final byte[] decodingTable;

    public CommonBaseEncoder(byte[] codingTable) {
        this(Padding.NORMAL, codingTable);
    }

    public CommonBaseEncoder(Padding paddingStrategy, byte[] codingTable) {
        this((byte) '-', (byte) '=', paddingStrategy, codingTable);
    }

    public CommonBaseEncoder(byte singlePadding, byte doublePadding, Padding paddingStrategy, byte[] encodingTable) {
        int codingSizeCheck = encodingTable.length - 1;
        if (codingSizeCheck > 0) {
            int codingSize = 0;
            while ((codingSizeCheck & 1) > 0) {
                codingSizeCheck >>>= 1;
                codingSize++;
            }
            if (codingSizeCheck > 0) {
                throw new InstantiationError("encodingTable size should be a positive power of two");
            }
            this.codingSize = codingSize;
        } else {
            throw new InstantiationError("encodingTable should not be empty");
        }
        this.singlePadding = singlePadding;
        this.doublePadding = doublePadding;
        this.paddingStrategy = paddingStrategy;
        this.encodingTable = encodingTable;
        this.decodingTable = createDecodingTable(encodingTable);
    }

    private static byte[] createDecodingTable(byte[] encodingTable) {
        int max = 0;
        for (byte d : encodingTable) {
            int di = d & BYTE_MASK;
            if (di > max) {
                max = di;
            }
        }
        byte[] decodingTable = new byte[max + 1];
        for (int i = 0; i < encodingTable.length; i++) {
            decodingTable[encodingTable[i] & BYTE_MASK] = (byte) i;
        }
        return decodingTable;
    }

    @Override
    public int encode(byte[] data, int off, CodeWriter out) {
        return encode(data, off, data.length - off, out);
    }

    @Override
    public int encode(byte[] data, CodeWriter out) {
        return encode(data, 0, data.length, out);
    }

    @Override
    public int decode(byte[] data, int off, CodeWriter out) throws ConversionException {
        return decode(data, off, data.length - off, out);
    }

    @Override
    public int decode(byte[] data, CodeWriter out) throws ConversionException {
        return decode(data, 0, data.length, out);
    }

    /**
     * encode the input data producing a base(power of 2) output.
     *
     * @return the number of bytes produced.
     */
    public int encode(byte[] data, int offset, int length, CodeWriter out) {
        checkBounds(data, offset, length);
        int writeCount = 0;
        {
            int d = 0, s = 0;
            for (int i = offset; i < offset + length; i++) {
                d <<= BYTE_SIZE;
                d |= data[i] & BYTE_MASK;
                s += BYTE_SIZE;
                while (s >= codingSize) {
                    out.write(encodingTable[d >>> (s -= codingSize)]);
                    writeCount++;
                    d &= (-1 << s) ^ -1;
                }
            }
            if (s > 0) {
                d <<= codingSize - s;
                out.write(encodingTable[d]);
                writeCount++;
                if (null != paddingStrategy) {
                    while ((s += BYTE_SIZE) % codingSize > 0) {
                        // Calculate paddingStrategy
                    }
                    switch (paddingStrategy) {
                    case NO_PADDING:
                        break;
                    case NORMAL:
                        while ((s -= codingSize) > 0) {
                            out.write(doublePadding);
                            writeCount++;
                        }
                        break;
                    case PACKED:
                        while ((s -= codingSize) > 0) {
                            if ((s -= codingSize) > 0) {
                                out.write(doublePadding);
                                writeCount++;
                            } else {
                                out.write(singlePadding);
                                writeCount++;
                            }
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown paddingStrategy: " + paddingStrategy);
                    }
                } else {
                    throw new IllegalArgumentException("Missing paddingStrategy");
                }
            }
        }
        return writeCount;
    }

    /**
     * decode the base(power of 2) encoded byte data, writing result to the given
     * output, whitespace characters will be ignored.
     *
     * @return the number of bytes produced.
     * @throws ConversionException
     */
    public int decode(byte[] data, int offset, int length, CodeWriter out) throws ConversionException {
        checkBounds(data, offset, length);
        int writeCount = 0;
        {
            int d = 0, s = 0;
            for (int i = offset; i < offset + length; i++) {
                d <<= codingSize;
                int c = data[i];
                if (c == doublePadding || c == singlePadding) {
                    if ((s + (codingSize * countPadding(data, i, length - i))) % BYTE_SIZE == 0) {
                        return writeCount;
                    } else {
                        throw new ConversionException("Padding missmatch");
                    }
                }
                d |= decodingTable[c & BYTE_MASK];
                s += codingSize;
                while (s >= BYTE_SIZE) {
                    out.write((byte) (d >>> (s -= BYTE_SIZE)));
                    writeCount++;
                    d &= (-1 << s) ^ -1;
                }
            }
        }
        return writeCount;
    }

    private int countPadding(byte[] data, int offset, int length) throws ConversionException {
        if (null == paddingStrategy) {
            throw new IllegalArgumentException("Missing paddingStrategy");
        }
        int countPadding = 0;
        switch (paddingStrategy) {
        case NO_PADDING:
            break;
        case NORMAL:
            for (int i = offset; i < offset + length; i++) {
                if (data[i] == doublePadding) {
                    countPadding++;
                } else {
                    throw new ConversionException("Wrong padding at " + i);
                }
            }
            break;
        case PACKED:
            boolean doublePaddingFinished = false;
            for (int i = offset; i < offset + length; i++) {
                if (data[i] == doublePadding && !doublePaddingFinished) {
                    countPadding += 2;
                } else if (data[i] == singlePadding) {
                    doublePaddingFinished = true;
                    countPadding++;
                } else {
                    throw new ConversionException("Wrong padding at " + i);
                }
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown paddingStrategy: " + paddingStrategy);
        }
        return countPadding;
    }

    private static void checkBounds(byte[] data, int offset, int length) {
        if (offset < 0) {
            throw new IndexOutOfBoundsException("offset = " + offset);
        }
        if (length <= 0) {
            if (length < 0) {
                throw new IndexOutOfBoundsException("length = " + length);
            }
            if (offset <= data.length) {
                return;
            }
        }
        // Note: offset or length might be near -1>>>1.
        if (offset > data.length - length) {
            throw new IndexOutOfBoundsException("offset + length = " + (offset + length));
        }
    }

    protected boolean ignore(char c) {
        switch (c) {
        case ' ':
        case '\r':
        case '\n':
        case '\t':
            return true;
        default:
            return false;
        }
    }

}
