package com.tiesdb.protocol.v0.api.context;

public interface MessageContext extends Context<MessageContext.Part> {

	enum Part {
		HEADER, ENTRY, QUERY
	}

	PacketContext getPacketContext();
}
