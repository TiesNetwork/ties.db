package com.tiesdb.protocol.v0.impl.context;

import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.Input;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.context.Context;
import com.tiesdb.protocol.v0.api.context.MessageContext;
import com.tiesdb.protocol.v0.api.context.PacketContext;
import com.tiesdb.protocol.v0.api.data.PacketHeader;
import com.tiesdb.protocol.v0.impl.TiesDBProtocolParser;
import com.tiesdb.protocol.v0.util.Synchronized;

public class PacketContextImpl extends ContextRoaming implements PacketContext {

	private PacketHeader header;
	private MessageContext messageContext;

	private Part next = Part.HEADER;
	private final ContextRoaming basicContext;

	public PacketContextImpl(ContextRoaming basicContext) {
		this.basicContext = basicContext;
	}

	@Override
	protected TiesDBProtocolParser getProtocolParser() {
		return basicContext.getProtocolParser();
	}

	@Override
	protected Synchronized<Input> getSynchronizedInput() {
		return basicContext.getSynchronizedInput();
	}

	@Override
	public Part next() throws TiesDBProtocolException {
		return next;
	}

	@Override
	public void parse() throws TiesDBProtocolException {
		switch (next) {
		case HEADER:
			header = getSynchronizedInput().sync(getProtocolParser()::parsePacketHeader);
			next = Part.MESSAGE;
			break;
		case MESSAGE:
			messageContext = new MessageContextImpl(this);
			next = null;
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
	public Context<?> blockedBy() {
		return messageContext != null && !messageContext.isClosed() ? messageContext : null;
	}

	@Override
	public PacketHeader getPacketHeader() {
		return header;
	}

	@Override
	public boolean isClosed() {
		return next == null;
	}

}
