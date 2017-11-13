package com.tiesdb.protocol.v0.impl;

import com.tiesdb.protocol.TiesDBProtocolPacketStream;
import com.tiesdb.protocol.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.TiesDBProtocolV0Parser;

public class TiesDBProtocolV0ParserImpl implements TiesDBProtocolV0Parser {

	private final TiesDBProtocolPacketStream packetStream;

	public TiesDBProtocolV0ParserImpl(TiesDBProtocolPacketStream packetStream) {
		this.packetStream = packetStream;
	}

	/**
	 * @see com.tiesdb.protocol.v0.api.TiesDBProtocolV0Parser#getMessageVersion(com.tiesdb.protocol.TiesDBProtocolPacketStream)
	 */
	@Override
	public Version getMessageVersion() throws TiesDBProtocolException {
		throw new TiesDBProtocolException("Packet is invalid or has incompatible format");
	}
}