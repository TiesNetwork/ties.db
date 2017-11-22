package com.tiesdb.protocol.v0.impl.message;

import com.tiesdb.protocol.TiesDBProtocolPacketChannel.Input;
import com.tiesdb.protocol.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.impl.util.Synchronized;

public class VersionParser extends Parser<TiesDBProtocolException, Version> {

	protected VersionParser(Synchronized<Input> inputWrapper) {
		super(inputWrapper);
	}

	@Override
	public Version apply(Input input) throws TiesDBProtocolException {
		byte[] data = new byte[6];
		for (int i = 0; i < data.length; i++) {
			data[i] = input.get();
		}
		int major = (((int) data[0] & 0xff) << 8) | ((int) data[1] & 0xff);
		int minor = (((int) data[2] & 0xff) << 8) | ((int) data[3] & 0xff);
		int maint = (((int) data[4] & 0xff) << 8) | ((int) data[5] & 0xff);

		return new Version(major, minor, maint);
	}

}
