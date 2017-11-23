package com.tiesdb.protocol.v0.api.data;

import com.tiesdb.protocol.api.data.Version;

public class PacketHeader {

	private final Version version;

	public PacketHeader(Version version) {
		this.version = version;
	}

	public Version getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return "PacketHeader [version=" + version + "]";
	}

}
