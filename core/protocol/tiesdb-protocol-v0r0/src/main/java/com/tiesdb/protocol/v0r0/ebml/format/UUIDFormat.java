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

import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import one.utopic.sparse.api.exception.SparseReaderException;
import one.utopic.sparse.ebml.EBMLFormat;
import one.utopic.sparse.ebml.format.BytesFormat;

/**
 * Writes and reads UUID data
 */
// TODO rewrite
public class UUIDFormat implements EBMLFormat<UUID> {

    private static final int REQUIRED_LENGTH = Long.BYTES * 2;
    public static final UUIDFormat INSTANCE = new UUIDFormat();

    @Override
    public UUID readFormat(byte[] data) {
        if (data.length != REQUIRED_LENGTH) {
            throw new SparseReaderException("Not enough bytes to construct UUID. Required " + REQUIRED_LENGTH + " but was " + data.length);
        }
        return UUID.fromString(DatatypeConverter.printHexBinary(data).replaceFirst("(.{8})(.{4})(.{4})(.{4})(.{12})", "\1-\2-\3-\4-\5"));
    }

    @Override
    public Writable getWritable(UUID data) {
        return BytesFormat.INSTANCE.getWritable(DatatypeConverter.parseHexBinary(data.toString().replaceAll("-", "")));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
