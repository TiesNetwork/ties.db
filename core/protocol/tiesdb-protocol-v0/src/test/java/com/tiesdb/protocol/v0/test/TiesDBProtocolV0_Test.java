package com.tiesdb.protocol.v0.test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tiesdb.protocol.api.TiesDBProtocol;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel;
import com.tiesdb.protocol.api.data.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.Conversation;
import com.tiesdb.protocol.v0.api.Handler;
import com.tiesdb.protocol.v0.api.context.PacketContext;
import com.tiesdb.protocol.v0.exception.CRCMissmatchException;
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
		Handler handler = mock(Handler.class);
		protocol.acceptPacket(channel, handler);
		verify(handler, times(1)).handle(isA(Conversation.class));
	}

	@Test
	@DisplayName("Protocol Version Parsing Success")
	void testProtocolVersionParsingSuccess() throws TiesDBProtocolException {
		fakeInput("C001 BA5E"//
				+ "3F01 57D1"//
				+ "0000 FFFF"//
				+ "0002 0001");
		Handler handler = spy(new Handler() {
			@Override
			public void handle(Conversation c) throws TiesDBProtocolException {
				PacketContext pc = c.getPacketContext();
				assertEquals(PacketContext.Part.HEADER, pc.next());
				pc.parse();
				assertEquals(new Version(65535, 2, 1), pc.getPacketHeader().getVersion());
			}
		});
		protocol.acceptPacket(channel, handler);
		verify(handler, times(1)).handle(isA(Conversation.class));
	}

	@Test
	@DisplayName("Protocol Version Parsing CRC Fail")
	void testProtocolVersionFailParsingCRCFail() throws TiesDBProtocolException {
		fakeInput("C001 BA5E"//
				+ "9398 C813"//
				+ "0000 FFFF"//
				+ "0002 0002");
		Handler handler = spy(new Handler() {
			@Override
			public void handle(Conversation c) throws TiesDBProtocolException {
				PacketContext pc = c.getPacketContext();
				assertEquals(PacketContext.Part.HEADER, pc.next());
				assertThrows(CRCMissmatchException.class, pc::parse);
			}
		});
		protocol.acceptPacket(channel, handler);
		verify(handler, times(1)).handle(isA(Conversation.class));
	}

	private void fakeInput(String hexString) {
		when(channel.getInput()).thenReturn(new HexStringInput(hexString));
	}
}
