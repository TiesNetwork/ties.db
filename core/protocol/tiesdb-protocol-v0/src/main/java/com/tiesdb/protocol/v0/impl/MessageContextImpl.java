package com.tiesdb.protocol.v0.impl;

import com.tiesdb.protocol.TiesDBProtocolPacketChannel;
import com.tiesdb.protocol.TiesDBProtocolPacketChannel.Input;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.MessageContext;
import com.tiesdb.protocol.v0.api.message.Message;
import com.tiesdb.protocol.v0.impl.message.MessageParser;
import com.tiesdb.protocol.v0.impl.util.Synchronized;

public class MessageContextImpl implements MessageContext {

	private MessageParser messageParser;

	public MessageContextImpl(TiesDBProtocolPacketChannel packetChannel) throws TiesDBProtocolException {
		this.messageParser = createMessageParser(createSynchronizedInput(packetChannel));
	}

	private MessageParser createMessageParser(Synchronized<Input> inputWrapper) {
		return new MessageParser(inputWrapper);
	}

	protected Synchronized<Input> createSynchronizedInput(TiesDBProtocolPacketChannel packetChannel) {
		return new Synchronized<>(packetChannel.getInput());
	}

	public Message getMessage() throws TiesDBProtocolException {
		return messageParser.get();
	}

}