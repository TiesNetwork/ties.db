package com.tiesdb.protocol.v0.impl;

import com.tiesdb.protocol.Version;
import com.tiesdb.protocol.v0.api.TiesDBProtocolV0;

public class TiesDBProtocolV0Impl implements TiesDBProtocolV0 {

	private static final Version version = new Version(0, 0, 0);

	@Override
	public Version getVersion() {
		return version;
	}

}
