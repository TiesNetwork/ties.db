package com.tiesdb.protocol.v0;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.tiesdb.protocol.TiesDBProtocol;
import com.tiesdb.protocol.TiesDBProtocolHandler;
import com.tiesdb.protocol.TiesDBProtocolPacketStream;
import com.tiesdb.protocol.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.TiesDBProtocolV0Handler;
import com.tiesdb.protocol.v0.api.TiesDBProtocolV0MessageContext;
import com.tiesdb.protocol.v0.impl.TiesDBProtocolV0Impl;

@DisplayName("TiesDBProtocol version 0 Test")
public class TiesDBProtocolV0_Test {

	private final TiesDBProtocol protocol = spy(new TiesDBProtocolV0Impl());

	@Test
	@DisplayName("Protocol MessageContext Handling")
	void testProtocolMessageContextHandling() throws TiesDBProtocolException {
		TiesDBProtocolV0Handler handler = mock(TiesDBProtocolV0Handler.class);
		protocol.acceptPacket(null, handler);
		verify(handler, times(1)).handle(isA(TiesDBProtocolV0MessageContext.class));
	}

	@Test
	@DisplayName("Protocol Version Parsing")
	void testProtocolVersionParsing() throws TiesDBProtocolException {
		TiesDBProtocolPacketStream stream = mock(TiesDBProtocolPacketStream.class);
		TiesDBProtocolV0Handler handler = spy(new TiesDBProtocolV0Handler() {
			@Override
			public void handle(TiesDBProtocolV0MessageContext context) throws TiesDBProtocolException {
				System.out.println(context.getMessageVersion());
			}
		});
		protocol.acceptPacket(stream, handler);
		verify(handler, times(1)).handle(isA(TiesDBProtocolV0MessageContext.class));
	}
}
