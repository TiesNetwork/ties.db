package com.tiesdb.protocol.v0.impl.message;

import com.tiesdb.protocol.TiesDBProtocolPacketChannel.Input;

import java.util.Arrays;

import com.tiesdb.lib.crypto.checksum.ChecksumManager;
import com.tiesdb.lib.crypto.checksum.api.Checksum;
import com.tiesdb.protocol.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.message.MessageHeader;
import com.tiesdb.protocol.v0.exception.CRCMissmatchException;
import com.tiesdb.protocol.v0.impl.util.Synchronized;

public class HeaderParser extends Parser<TiesDBProtocolException, MessageHeader> {

	private static final byte[] MAGIC_NUMBER = new byte[] { (byte) 0xc0, 0x01, (byte) 0xba, 0x5e };

	protected HeaderParser(Synchronized<Input> inputWrapper) {
		super(inputWrapper);
	}

	Field<Version> version = lazy(VersionParser::new);

	@Override
	public MessageHeader apply(Input input) throws TiesDBProtocolException {
		checkIntegrity(input);
		input.peekStart();
		byte[] data = new byte[MAGIC_NUMBER.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = input.get();
		}
		if (Arrays.equals(data, MAGIC_NUMBER)) {
			input.peekSkip();
		} else {
			input.peekRewind();
			throw new TiesDBProtocolException("Wrong magic number");
		}
		input.seek(2); // RESERVED BYTES
		return new MessageHeader() {
			@Override
			public Version getVersion() throws TiesDBProtocolException {
				return version.get();
			}
		};
	}

	private void checkIntegrity(Input input) throws TiesDBProtocolException {
		input.peekStart();
		Checksum checksum = ChecksumManager.getChecksum(ChecksumManager.CRC32);
		byte[] data = new byte[12];
		for (int i = 0; i < data.length; i++) {
			byte b = input.get();
			data[i] = b;
			checksum.update(b);
		}
		long compCRC = checksum.getValue();
		long dataCRC = 0;
		for (int i = 3; i >= 0; i--) {
			dataCRC |= ((long) input.get() & 0xff) << (8 * i);
		}
		if (compCRC != dataCRC) {
			throw new CRCMissmatchException("CRC check failed in Header");
		}
		input.peekRewind();
	}

}
