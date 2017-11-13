package com.tiesdb.protocol.v0.impl;

import com.tiesdb.protocol.TiesDBProtocol;
import com.tiesdb.protocol.TiesDBProtocolHandler;
import com.tiesdb.protocol.TiesDBProtocolPacketStream;
import com.tiesdb.protocol.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.TiesDBProtocolV0MessageContext;
import com.tiesdb.protocol.v0.api.TiesDBProtocolV0Parser;
import com.tiesdb.protocol.v0.api.TiesDBProtocolV0Handler;
import static com.google.common.base.Preconditions.*;

public class TiesDBProtocolV0Impl implements TiesDBProtocol {

	private static final Version version = new Version(0, 0, 1);

	@Override
	public Version getVersion() {
		return version;
	}

	@Override
	public void acceptPacket(TiesDBProtocolPacketStream packetStream, TiesDBProtocolHandler handler)
			throws TiesDBProtocolException {
		checkNotNull(handler);
		checkArgument(handler instanceof TiesDBProtocolV0Handler);
		TiesDBProtocolV0MessageContext context = createContext(packetStream);
		((TiesDBProtocolV0Handler) handler).handle(context);
	}

	protected TiesDBProtocolV0MessageContext createContext(TiesDBProtocolPacketStream packetStream) {
		return new TiesDBProtocolV0MessageContextImpl(createParser(packetStream));
	}

	protected TiesDBProtocolV0Parser createParser(TiesDBProtocolPacketStream packetStream) {
		return new TiesDBProtocolV0ParserImpl(packetStream);
	}

}
