package com.tiesdb.protocol.v0.impl;

import com.tiesdb.protocol.TiesDBProtocol;
import com.tiesdb.protocol.TiesDBProtocolHandler;
import com.tiesdb.protocol.TiesDBProtocolPacketChannel;
import com.tiesdb.protocol.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.MessageContext;
import com.tiesdb.protocol.v0.api.ProtocolHandler;
import static com.google.common.base.Preconditions.*;

public class TiesDBProtocolImpl implements TiesDBProtocol {

	private static final Version version = new Version(0, 0, 1);

	@Override
	public Version getVersion() {
		return version;
	}

	@Override
	public void acceptPacket(TiesDBProtocolPacketChannel packetChannel, TiesDBProtocolHandler handler)
			throws TiesDBProtocolException {
		checkNotNull(handler);
		checkArgument(handler instanceof ProtocolHandler);
		MessageContext context = createContext(packetChannel);
		((ProtocolHandler) handler).handle(context);
	}

	protected MessageContext createContext(TiesDBProtocolPacketChannel packetChannel) throws TiesDBProtocolException {
		return new MessageContextImpl(packetChannel);
	}

}
