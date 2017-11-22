package com.tiesdb.protocol.v0.test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tiesdb.protocol.TiesDBProtocol;
import com.tiesdb.protocol.TiesDBProtocolPacketChannel;
import com.tiesdb.protocol.TiesDBProtocolPacketChannel.Input;
import com.tiesdb.protocol.TiesDBProtocolPacketChannel.State;
import com.tiesdb.protocol.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.ProtocolHandler;
import com.tiesdb.protocol.v0.exception.CRCMissmatchException;
import com.tiesdb.protocol.v0.api.MessageContext;
import com.tiesdb.protocol.v0.impl.TiesDBProtocolImpl;
import com.tiesdb.protocol.v0.test.util.HexStringInput;

@DisplayName("TiesDBProtocol version 0 Test")
public class TiesDBProtocolV0_Test {

	private final TiesDBProtocol protocol = spy(new TiesDBProtocolImpl());
	private final TiesDBProtocolPacketChannel channel;
	{
		channel = mock(TiesDBProtocolPacketChannel.class);
		when(channel.getInput()).thenReturn(mock(TiesDBProtocolPacketChannel.Input.class));
	}

	@Test
	@DisplayName("Protocol MessageContext Handling")
	void testProtocolMessageContextHandling() throws TiesDBProtocolException {
		ProtocolHandler handler = mock(ProtocolHandler.class);
		protocol.acceptPacket(channel, handler);
		verify(handler, times(1)).handle(isA(MessageContext.class));
	}

	@Test
	@DisplayName("Protocol Version Parsing Success")
	void testProtocolVersionParsingSuccess() throws TiesDBProtocolException {
		fakeInput("C001 BA5E"//
				+ "0000 FFFF"//
				+ "0002 0001"//
				+ "9398 C813");
		ProtocolHandler handler = spy(new ProtocolHandler() {
			@Override
			public void handle(MessageContext context) throws TiesDBProtocolException {
				assertEquals(new Version(65535, 2, 1), context.getMessage().getHeader().getVersion());
			}
		});
		protocol.acceptPacket(channel, handler);
		verify(handler, times(1)).handle(isA(MessageContext.class));
	}

	@Test
	@DisplayName("Protocol Version Parsing Success2")
	void testProtocolVersionParsingSuccess2() throws TiesDBProtocolException {
		fakeInput("C001 BA5E"//
				+ "0000 0001"//
				+ "0002 0003"//
				+ "6E56 ECCF");
		ProtocolHandler handler = spy(new ProtocolHandler() {
			@Override
			public void handle(MessageContext context) throws TiesDBProtocolException {
				assertEquals(new Version(1, 2, 3), context.getMessage().getHeader().getVersion());
			}
		});
		protocol.acceptPacket(channel, handler);
		verify(handler, times(1)).handle(isA(MessageContext.class));
	}

	@Test
	@DisplayName("Protocol Version Parsing CRC Fail")
	void testProtocolVersionFailParsingCRCFail() throws TiesDBProtocolException {
		fakeInput("C001 BA5E"//
				+ "0000 FFFF"//
				+ "0002 0002"//
				+ "9398 C813");
		ProtocolHandler handler = spy(new ProtocolHandler() {
			@Override
			public void handle(MessageContext context) throws TiesDBProtocolException {
				assertThrows(CRCMissmatchException.class, context.getMessage()::getHeader);
			}
		});
		protocol.acceptPacket(channel, handler);
		verify(handler, times(1)).handle(isA(MessageContext.class));
	}

	private void fakeInput(String hexString) {
		when(channel.getInput()).thenReturn(new HexStringInput(hexString));
	}
}
