package com.tiesdb.protocol.v0.impl.context;

import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.Input;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.context.Context;
import com.tiesdb.protocol.v0.api.context.MessageContext;
import com.tiesdb.protocol.v0.api.context.PacketContext;
import com.tiesdb.protocol.v0.api.data.MessageHeader;
import com.tiesdb.protocol.v0.impl.TiesDBProtocolParser;
import com.tiesdb.protocol.v0.util.Synchronized;

public class MessageContextImpl extends ContextRoaming implements MessageContext {

	private MessageHeader header;

	private Part next = null; // Part.HEADER;
	private final PacketContextImpl packetContext;

	public MessageContextImpl(PacketContextImpl packetContext) {
		this.packetContext = packetContext;
	}

	public MessageHeader getHeader() {
		return this.header;
	}

	@Override
	public Part next() throws TiesDBProtocolException {
		return this.next;
	}

	@Override
	public void parse() throws TiesDBProtocolException {
		switch (next) {
		case HEADER:
			header = getSynchronizedInput().sync(getProtocolParser()::parseMessageHeader);
			break;
		case ENTRY:
			next = null;
			break;
		case QUERY:
			next = null;
			break;
		default:
			next = null;
			break;
		}
	}

	@Override
	public void skip() throws TiesDBProtocolException {
		parse();
	}

	@Override
	public PacketContext getPacketContext() {
		return packetContext;
	}

	@Override
	public Context<?> blockedBy() {
		return null;
	}

	@Override
	public Synchronized<Input> getSynchronizedInput() {
		return packetContext.getSynchronizedInput();
	}

	@Override
	public TiesDBProtocolParser getProtocolParser() {
		return packetContext.getProtocolParser();
	}

	@Override
	public boolean isClosed() {
		return next == null;
	}

}
