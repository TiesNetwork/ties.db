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
package com.tiesdb.protocol.v0r0.ebml.format;

import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import com.tiesdb.protocol.v0r0.ebml.TiesDBRequestConsistency;
import com.tiesdb.protocol.v0r0.ebml.TiesDBRequestConsistency.ConsistencyType;

import one.utopic.abio.api.output.Output;
import one.utopic.sparse.api.exception.SparseReaderException;
import one.utopic.sparse.ebml.EBMLFormat;

/**
 * <pre>
 * CONSISTENCY
 * -------------------------------
 * 0x00 (0) = Quorum
 * -------------------------------
 * 0x01 (1) = 1%
 * ...
 * 0x63 (99) = 99%
 * 0x64 (100) = All (100%)
 * -------------------------------
 * 0x65
 * ...        RESERVED
 * 0x9B
 * -------------------------------
 * 0x9C (-100) = 100
 * ...
 * 0xFD (-3) = 3
 * 0xFE (-2) = 2
 * 0xFF (-1) = 1
 * -------------------------------
 * </pre>
 */
public class TiesDBRequestConsistencyFormat implements EBMLFormat<TiesDBRequestConsistency> {

    public static final TiesDBRequestConsistencyFormat INSTANCE = new TiesDBRequestConsistencyFormat();

    @Override
    public TiesDBRequestConsistency readFormat(byte[] data) {
        if (data.length <= 0) {
            throw new SparseReaderException("Illegal TiesDBRequestConsistency " + DatatypeConverter.printHexBinary(data));
        }
        byte cb = data[data.length - 1];
        ConsistencyType type = null;
        Integer value = null;
        if (0 == cb) {
            type = ConsistencyType.QUORUM;
            value = 0;
        } else if (cb > 0 && cb < 101) {
            type = ConsistencyType.PERCENT;
            value = 0 + cb;
        } else if (cb < 0 && cb > -101) {
            type = ConsistencyType.COUNT;
            value = 0 - cb;
        } else {
            throw new SparseReaderException("Illegal TiesDBRequestConsistency " + DatatypeConverter.printHexBinary(data));
        }
        return new TiesDBRequestConsistency(type, value);
    }

    @Override
    public Writable getWritable(TiesDBRequestConsistency c) {
        return new Writable() {

            @Override
            public void writeFormat(Output out) throws IOException {
                byte b = 0;
                ConsistencyType cType = c.getType();
                switch (cType) {
                case COUNT:
                    b |= Byte.MIN_VALUE;
                    b |= 0 - c.getValue();
                    break;
                case PERCENT:
                    b |= c.getValue();
                    break;
                case QUORUM:
                    break;
                default:
                    throw new IOException("Unknown consystency type " + cType);
                }
                out.writeByte(b);
            }

            @Override
            public int getSize() {
                return 1;
            }

        };
    }

}
