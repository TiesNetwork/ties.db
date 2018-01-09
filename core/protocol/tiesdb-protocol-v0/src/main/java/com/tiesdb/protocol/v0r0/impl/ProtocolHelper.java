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
package com.tiesdb.protocol.v0r0.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.tiesdb.lib.crypto.checksum.ChecksumManager;
import com.tiesdb.lib.crypto.checksum.api.Checksum;
import com.tiesdb.protocol.api.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.exception.CRCMissmatchException;
import com.tiesdb.protocol.v0r0.util.DefaultHelper;

import one.utopic.abio.api.input.Input;
import one.utopic.abio.api.output.Output;

public class ProtocolHelper {

	public static final class Default {
		public static final ProtocolHelper INSTANCE = new ProtocolHelper();
		static {
			DefaultHelper.trace("Loaded default {}", INSTANCE);
		}
	}

	private static final byte[] PACKET_HEADER_MAGIC_NUMBER = new byte[] { (byte) 0xc0, 0x01, (byte) 0xba, 0x5e };
	private static final int PACKET_HEADER_RESERVED_LEN = 2;

	public Version parsePacketHeader(Input in) throws TiesDBProtocolException, IOException {
		{
			byte[] data = new byte[PACKET_HEADER_MAGIC_NUMBER.length];
			for (int i = 0; i < data.length; i++) {
				data[i] = in.readByte();
			}
			if (!Arrays.equals(data, PACKET_HEADER_MAGIC_NUMBER)) {
				throw new TiesDBProtocolException("Wrong packet magic number");
			}
		}
		Version version;
		{
			long dataCRC = parseLong32(in::readByte);
			Checksum checksum = ChecksumManager.getChecksum(ChecksumManager.CRC32);
			CheckedSupplier<Byte, IOException> sup = () -> checksum.updateVal(in.readByte());
			skip(sup, PACKET_HEADER_RESERVED_LEN);
			int major = parseInt16(sup);
			int minor = parseInt16(sup);
			int maint = parseInt16(sup);
			if (checksum.getValue() != dataCRC) {
				throw new CRCMissmatchException("CRC check failed in packet header");
			}
			version = new Version(major, minor, maint);
		}
		return version;
	}

	public void writePacketHeader(Version v, Output out) throws IOException {
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

	/* UTILITY FUNCTIONS */

	protected static void writeBytes(Output out, byte[] buf) throws IOException {
		for (int i = 0; i < buf.length; i++) {
			out.writeByte(buf[i]);
		}
	}

	protected static void writeLong32(CheckedConsumer<Byte, IOException> out, long value) throws IOException {
		writeLong(out, value, 4);
	}

	protected static void writeInt16(CheckedConsumer<Byte, IOException> out, int value) throws IOException {
		writeLong(out, value, 2);
	}

	protected static void writeLong(CheckedConsumer<Byte, IOException> out, long value, int bytes) throws IOException {
		for (int i = bytes; i > 0; --i) {
			out.accept((byte) (0xFF & (value >>> (8 * (i - 1)))));
		}
	}

	protected static void skip(CheckedSupplier<Byte, IOException> sup, int bytes) throws IOException {
		for (int i = bytes; i > 0; --i) {
			sup.get();
		}
	}

	protected static long parseLong64(CheckedSupplier<Byte, IOException> sup) throws IOException {
		return parseLong(sup, 8);
	}

	protected static long parseLong32(CheckedSupplier<Byte, IOException> sup) throws IOException {
		return parseLong(sup, 4);
	}

	protected static int parseInt32(CheckedSupplier<Byte, IOException> sup) throws IOException {
		return parseInt(sup, 4);
	}

	protected static int parseInt16(CheckedSupplier<Byte, IOException> sup) throws IOException {
		return parseInt(sup, 2);
	}

	protected static int parseInt(CheckedSupplier<Byte, IOException> sup, int bytes) throws IOException {
		int value = 0;
		for (int i = bytes; i > 0; --i) {
			value |= ((int) sup.get() & 0xff) << (8 * (i - 1));
		}
		return value;
	}

	protected static long parseLong(CheckedSupplier<Byte, IOException> sup, int bytes) throws IOException {
		long value = 0;
		for (int i = bytes; i > 0; --i) {
			value |= ((long) sup.get() & 0xff) << (8 * (i - 1));
		}
		return value;
	}

}

@FunctionalInterface
interface CheckedSupplier<T, E extends Throwable> {
	T get() throws E;
}

@FunctionalInterface
interface CheckedConsumer<T, E extends Throwable> {
	void accept(T v) throws E;
}
