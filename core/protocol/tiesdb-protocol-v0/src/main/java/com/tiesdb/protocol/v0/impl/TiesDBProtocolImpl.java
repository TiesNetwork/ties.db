package com.tiesdb.protocol.v0.impl;

import java.util.Objects;

import com.tiesdb.protocol.api.TiesDBProtocol;
import com.tiesdb.protocol.api.TiesDBProtocolHandler;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel;
import com.tiesdb.protocol.api.data.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.Conversation;
import com.tiesdb.protocol.v0.api.Handler;
import com.tiesdb.protocol.v0.impl.context.ConversationImpl;

public class TiesDBProtocolImpl implements TiesDBProtocol {

	private static final Version version = new Version(0, 0, 1);

	@Override
	public Version getVersion() {
		return version;
	}

	@Override
	public void acceptPacket(TiesDBProtocolPacketChannel packetChannel, TiesDBProtocolHandler handler)
			throws TiesDBProtocolException {
		Objects.requireNonNull(packetChannel);
		Objects.requireNonNull(handler);
		if (!Handler.class.isInstance(handler)) {
			throw new IllegalArgumentException(
					"Handler of " + handler.getClass() + " should implement an " + Handler.class);
		}
		((Handler) handler).handle(createConversation(packetChannel));
	}

	private Conversation createConversation(TiesDBProtocolPacketChannel packetChannel) {
		return new ConversationImpl(packetChannel);
	}

}
