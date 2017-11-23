package com.tiesdb.protocol.v0.impl.context;

import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.Input;
import com.tiesdb.protocol.v0.api.Conversation;
import com.tiesdb.protocol.v0.api.context.Context;
import com.tiesdb.protocol.v0.api.context.PacketContext;
import com.tiesdb.protocol.v0.impl.TiesDBProtocolParser;
import com.tiesdb.protocol.v0.util.Synchronized;

public class ConversationImpl implements Conversation {

	private final TiesDBProtocolPacketChannel packetChannel;

	private PacketContext packetContext;

	public ConversationImpl(TiesDBProtocolPacketChannel packetChannel) {
		this.packetChannel = packetChannel;
	}

	protected PacketContext createPacketContext(BasicContext basicContext) {
		return new PacketContextImpl(basicContext);
	}

	protected BasicContext createBasicContext(TiesDBProtocolPacketChannel packetChannel) {
		return new BasicContext(createSynchronizedInput(packetChannel.getInput()), createProtocolParser());
	}

	protected TiesDBProtocolParser createProtocolParser() {
		return new TiesDBProtocolParser();
	}

	protected Synchronized<Input> createSynchronizedInput(Input input) {
		return new Synchronized<>(input);
	}

	@Override
	public PacketContext getPacketContext() {
		return this.packetContext != null
			? this.packetContext
			: (this.packetContext = createPacketContext(createBasicContext(packetChannel)));
	}

	protected static class BasicContext extends ContextRoaming {

		private final Synchronized<Input> synchronizedInput;
		private final TiesDBProtocolParser protocolParser;

		public Synchronized<Input> getSynchronizedInput() {
			return synchronizedInput;
		}

		public TiesDBProtocolParser getProtocolParser() {
			return protocolParser;
		}

		@Override
		public Context<?> blockedBy() {
			return null;
		}

		public BasicContext(Synchronized<Input> synchronizedInput, TiesDBProtocolParser protocolParser) {
			this.synchronizedInput = synchronizedInput;
			this.protocolParser = protocolParser;
		}

	}

}
