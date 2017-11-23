package com.tiesdb.protocol.v0.impl;

import java.util.Arrays;
import java.util.function.Supplier;

import com.tiesdb.lib.crypto.checksum.ChecksumManager;
import com.tiesdb.lib.crypto.checksum.api.Checksum;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.Input;
import com.tiesdb.protocol.api.data.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.data.MessageHeader;
import com.tiesdb.protocol.v0.api.data.PacketHeader;
import com.tiesdb.protocol.v0.exception.CRCMissmatchException;

public class TiesDBProtocolParser extends ParserUtils {

	private static final byte[] PACKET_HEADER_MAGIC_NUMBER = new byte[] { (byte) 0xc0, 0x01, (byte) 0xba, 0x5e };
	private static final int PACKET_HEADER_RESERVED_LEN = 2;

	public PacketHeader parsePacketHeader(Input input) throws TiesDBProtocolException {
		{
			byte[] data = new byte[PACKET_HEADER_MAGIC_NUMBER.length];
			for (int i = 0; i < data.length; i++) {
				data[i] = input.get();
			}
			if (!Arrays.equals(data, PACKET_HEADER_MAGIC_NUMBER)) {
				throw new TiesDBProtocolException("Wrong packet magic number");
			}
		}
		Version version;
		{
			long dataCRC = parseLong32(input::get);
			Checksum checksum = ChecksumManager.getChecksum(ChecksumManager.CRC32);
			Supplier<Byte> sup = () -> checksum.updateVal(input.get());
			skip(sup, PACKET_HEADER_RESERVED_LEN);
			int major = parseInt16(sup);
			int minor = parseInt16(sup);
			int maint = parseInt16(sup);
			if (checksum.getValue() != dataCRC) {
				throw new CRCMissmatchException("CRC check failed in packet header");
			}
			version = new Version(major, minor, maint);
		}
		return new PacketHeader(version);
	}

	public MessageHeader parseMessageHeader(Input input) throws TiesDBProtocolException {
		return null;
	}

}

class ParserUtils {

	protected void skip(Supplier<Byte> sup, int bytes) {
		for (int i = bytes; i > 0; --i) {
			sup.get();
		}
	}

	protected long parseLong64(Supplier<Byte> sup) {
		return parseLong(sup, 8);
	}

	protected long parseLong32(Supplier<Byte> sup) {
		return parseLong(sup, 4);
	}

	protected int parseInt32(Supplier<Byte> sup) {
		return parseInt(sup, 4);
	}

	protected int parseInt16(Supplier<Byte> sup) {
		return parseInt(sup, 2);
	}

	protected int parseInt(Supplier<Byte> sup, int bytes) {
		int value = 0;
		for (int i = bytes; i > 0; --i) {
			value |= ((int) sup.get() & 0xff) << (8 * (i - 1));
		}
		return value;
	}

	protected long parseLong(Supplier<Byte> sup, int bytes) {
		long value = 0;
		for (int i = bytes; i > 0; --i) {
			value |= ((long) sup.get() & 0xff) << (8 * (i - 1));
		}
		return value;
	}
}
