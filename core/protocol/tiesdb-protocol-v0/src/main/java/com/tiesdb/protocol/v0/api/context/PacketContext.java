package com.tiesdb.protocol.v0.api.context;

import com.tiesdb.protocol.api.data.Version;
import com.tiesdb.protocol.v0.api.data.PacketHeader;

public interface PacketContext extends Context<PacketContext.Part> {

	enum Part {
		HEADER, MESSAGE
	}

	PacketHeader getPacketHeader();
}
