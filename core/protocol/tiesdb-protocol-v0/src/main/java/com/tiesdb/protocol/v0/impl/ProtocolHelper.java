package com.tiesdb.protocol.v0.impl;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.tiesdb.lib.crypto.checksum.ChecksumManager;
import com.tiesdb.lib.crypto.checksum.api.Checksum;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.Input;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.Output;
import com.tiesdb.protocol.api.data.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.exception.CRCMissmatchException;
import com.tiesdb.protocol.v0.util.DefaultHelper;

public class ProtocolHelper {

	public static final class Default {
		public static final ProtocolHelper INSTANCE = new ProtocolHelper();
		static {
			DefaultHelper.trace("Loaded default {}", INSTANCE);
		}
	}

	private static final byte[] PACKET_HEADER_MAGIC_NUMBER = new byte[] { (byte) 0xc0, 0x01, (byte) 0xba, 0x5e };
	private static final int PACKET_HEADER_RESERVED_LEN = 2;

	public Version parsePacketHeader(Input in) throws TiesDBProtocolException {
		{
			byte[] data = new byte[PACKET_HEADER_MAGIC_NUMBER.length];
			for (int i = 0; i < data.length; i++) {
				data[i] = in.get();
			}
			if (!Arrays.equals(data, PACKET_HEADER_MAGIC_NUMBER)) {
				throw new TiesDBProtocolException("Wrong packet magic number");
			}
		}
		Version version;
		{
			long dataCRC = parseLong32(in::get);
			Checksum checksum = ChecksumManager.getChecksum(ChecksumManager.CRC32);
			Supplier<Byte> sup = () -> checksum.updateVal(in.get());
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

	public void writePacketHeader(Version v, Output out) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Checksum checksum = ChecksumManager.getChecksum(ChecksumManager.CRC32);
		Consumer<Byte> con = (b) -> {
			checksum.update(b);
			baos.write(b);
		};
		for (int i = 0; i < PACKET_HEADER_RESERVED_LEN; i++) {
			con.accept((byte) 0);
		}
		writeInt16(con, v.getMajor());
		writeInt16(con, v.getMinor());
		writeInt16(con, v.getMaint());

		writeBytes(out, PACKET_HEADER_MAGIC_NUMBER);
		writeLong32((b) -> out.put(b), checksum.getValue());
		writeBytes(out, baos.toByteArray());
	}

	/* UTILITY FUNCTIONS */

	protected static void writeBytes(Output out, byte[] buf) {
		for (int i = 0; i < buf.length; i++) {
			out.put(buf[i]);
		}
	}

	protected static void writeLong32(Consumer<Byte> out, long value) {
		writeLong(out, value, 4);
	}

	protected static void writeInt16(Consumer<Byte> out, int value) {
		writeLong(out, value, 2);
	}

	protected static void writeLong(Consumer<Byte> out, long value, int bytes) {
		for (int i = bytes; i > 0; --i) {
			out.accept((byte) (0xFF & (value >>> (8 * (i - 1)))));
		}
	}

	protected static void skip(Supplier<Byte> sup, int bytes) {
		for (int i = bytes; i > 0; --i) {
			sup.get();
		}
	}

	protected static long parseLong64(Supplier<Byte> sup) {
		return parseLong(sup, 8);
	}

	protected static long parseLong32(Supplier<Byte> sup) {
		return parseLong(sup, 4);
	}

	protected static int parseInt32(Supplier<Byte> sup) {
		return parseInt(sup, 4);
	}

	protected static int parseInt16(Supplier<Byte> sup) {
		return parseInt(sup, 2);
	}

	protected static int parseInt(Supplier<Byte> sup, int bytes) {
		int value = 0;
		for (int i = bytes; i > 0; --i) {
			value |= ((int) sup.get() & 0xff) << (8 * (i - 1));
		}
		return value;
	}

	protected static long parseLong(Supplier<Byte> sup, int bytes) {
		long value = 0;
		for (int i = bytes; i > 0; --i) {
			value |= ((long) sup.get() & 0xff) << (8 * (i - 1));
		}
		return value;
	}

}
