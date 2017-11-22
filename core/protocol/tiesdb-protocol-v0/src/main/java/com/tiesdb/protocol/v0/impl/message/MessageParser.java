package com.tiesdb.protocol.v0.impl.message;

import com.tiesdb.protocol.TiesDBProtocolPacketChannel.Input;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.message.Message;
import com.tiesdb.protocol.v0.api.message.MessageHeader;
import com.tiesdb.protocol.v0.impl.util.Synchronized;

public class MessageParser extends Parser<TiesDBProtocolException, Message> {

	public MessageParser(Synchronized<Input> inputWrapper) {
		super(inputWrapper);
	}

	Field<MessageHeader> header = lazy(HeaderParser::new);

	@Override
	public Message apply(Input arguments) throws TiesDBProtocolException {
		return new Message() {
			@Override
			public MessageHeader getHeader() throws TiesDBProtocolException {
				return header.get();
			}
		};
	}

}
