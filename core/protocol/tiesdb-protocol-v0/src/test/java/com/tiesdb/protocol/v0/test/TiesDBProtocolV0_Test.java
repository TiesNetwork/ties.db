/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project.If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.protocol.v0.test;

import static org.junit.jupiter.api.Assertions.*;
import static com.tiesdb.protocol.v0.test.util.TestHelper.*;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tiesdb.protocol.TiesDBProtocolManager;
import com.tiesdb.protocol.api.TiesDBProtocol;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel;
import com.tiesdb.protocol.api.data.ElementReader;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.TiesDBProtocolV0;
import com.tiesdb.protocol.v0.api.ConsistencyLevel;
import com.tiesdb.protocol.v0.api.TiesConversation;
import com.tiesdb.protocol.v0.api.TiesConversationHandler;
import com.tiesdb.protocol.v0.api.TiesElement;
import com.tiesdb.protocol.v0.element.TiesDBBaseRequest;
import com.tiesdb.protocol.v0.element.TiesDBEntry;
import com.tiesdb.protocol.v0.element.TiesDBModificationRequest;
import com.tiesdb.protocol.v0.element.TiesDBRequestConsistency;
import com.tiesdb.protocol.v0.element.TiesDBRequestSignature;
import com.tiesdb.protocol.v0.exception.CRCMissmatchException;
import com.tiesdb.protocol.v0.impl.ElementFactory;
import com.tiesdb.protocol.v0.impl.ProtocolHelper;

@DisplayName("TiesDBProtocol version 0 Test")
public class TiesDBProtocolV0_Test {

	private static final List<TiesDBProtocol> PROTOCOLS = TiesDBProtocolManager.loadProtocols();

	private static final TiesDBProtocolPacketChannel createChannel() {
		TiesDBProtocolPacketChannel channel = mock(TiesDBProtocolPacketChannel.class);
		when(channel.getInput()).thenReturn(mock(TiesDBProtocolPacketChannel.Input.class));
		return channel;
	}

	@Test
	@DisplayName("Protocol MessageContext Handling")
	void testProtocolMessageContextHandling() throws TiesDBProtocolException {
		TiesDBProtocol protocol = createProtocol();
		TiesDBProtocolPacketChannel channel = createChannel();
		TiesConversationHandler tiesConversationHandler = mock(TiesConversationHandler.class);
		protocol.createChannel(channel, tiesConversationHandler);
		verify(tiesConversationHandler, times(1)).handle(isA(TiesConversation.class));
	}

	private TiesDBProtocol createProtocol() {
		return spy(PROTOCOLS.get(0));
	}

	@Test
	@DisplayName("Protocol Version Parsing Success")
	void testProtocolVersionParsingSuccess() throws TiesDBProtocolException, IOException {

		ElementFactory ef = new ElementFactory();
		ProtocolHelper ph = new ProtocolHelper();

		TiesDBProtocolV0 protocol = spy(new TiesDBProtocolV0(ef, ph));

		TiesDBModificationRequest request = new TiesDBModificationRequest();
		TiesDBRequestSignature signature = new TiesDBRequestSignature();
		signature.setValue("Hello Test!".getBytes());
		request.setSignature(signature);
		TiesDBRequestConsistency consistency = new TiesDBRequestConsistency();
		consistency.setValue(ConsistencyLevel.ALL);
		request.setConsistency(consistency);
		TiesDBEntry entry = new TiesDBEntry();
		entry.setValue("Yahoooooooooooooooooo!!!".getBytes());
		request.setEntry(entry);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		{
			TiesDBProtocolPacketChannel channel = createChannel();
			fakeOutput(out, channel);
			TiesConversationHandler writeHandler = spy(new TiesConversationHandler() {
				@Override
				public void handle(TiesConversation c) throws TiesDBProtocolException {
					c.getWriter().write(request);
				}
			});

			assertTrue(protocol.createChannel(channel, writeHandler));
			verify(channel, times(0)).getInput();
			verify(channel, times(1)).getOutput();
			verify(writeHandler, times(1)).handle(isA(TiesConversation.class));
		}

		ByteArrayOutputStream outCheck = new ByteArrayOutputStream();
		AtomicReference<TiesElement> requestCheckContainer = new AtomicReference<TiesElement>();
		{
			TiesDBProtocolPacketChannel channel = createChannel();
			fakeInput(out.toString(), channel);
			fakeOutput(outCheck, channel);
			TiesConversationHandler echoHandler = spy(new TiesConversationHandler() {

				@Override
				public void handle(TiesConversation c) throws TiesDBProtocolException {
					ElementReader<TiesElement> reader = c.getReader();
					TiesElement requestCheck = null;
					while (reader.hasNext()) {
						if (requestCheck == null) {
							requestCheckContainer.set(requestCheck = reader.readNext());
						} else {
							reader.readNext();
						}
					}
					c.getWriter().write(requestCheck);
				}

			});
			assertTrue(protocol.acceptChannel(channel, echoHandler));
			verify(channel, times(2)).getInput();
			verify(channel, times(1)).getOutput();
			verify(echoHandler, times(1)).handle(isA(TiesConversation.class));
		}
		assertEquals(request, requestCheckContainer.get());
		assertDeepEquals(request, requestCheckContainer.get());
		assertArrayEquals(out.toByteArray(), outCheck.toByteArray());
	}

	@Test
	@DisplayName("Protocol Version Parsing Success Different Version")
	void testProtocolVersionParsingSuccessDifferentVersion() throws TiesDBProtocolException {
		TiesDBProtocol protocol = createProtocol();
		TiesDBProtocolPacketChannel channel = createChannel();
		TiesConversationHandler tiesConversationHandler = mock(TiesConversationHandler.class);
		fakeInput("C001 BA5E"//
				+ "DB04 CD96"//
				+ "0000 0000"//
				+ "0000 FFFF"//
				, channel);
		assertTrue(protocol.acceptChannel(channel, tiesConversationHandler));
		verify(channel, times(1)).getInput();
		verify(tiesConversationHandler, times(1)).handle(isA(TiesConversation.class));
	}

	@Test
	@DisplayName("Protocol Version Parsing Fail Different Version")
	void testProtocolVersionParsingFailDifferentVersion() throws TiesDBProtocolException {
		TiesDBProtocol protocol = createProtocol();
		TiesDBProtocolPacketChannel channel = createChannel();
		TiesConversationHandler tiesConversationHandler = mock(TiesConversationHandler.class);
		fakeInput("C001 BA5E"//
				+ "13E7 85C8"//
				+ "0000 0000"//
				+ "0001 0001"//
				, channel);
		assertFalse(protocol.acceptChannel(channel, tiesConversationHandler));
		verify(channel, times(1)).getInput();
		verify(tiesConversationHandler, times(0)).handle(isA(TiesConversation.class));
	}

	@Test
	@DisplayName("Protocol Version Parsing Fail Wrong CRC")
	void testProtocolVersionParsingFailWrongCRC() throws TiesDBProtocolException {
		TiesDBProtocol protocol = createProtocol();
		TiesDBProtocolPacketChannel channel = createChannel();
		TiesConversationHandler tiesConversationHandler = mock(TiesConversationHandler.class);
		fakeInput("C001 BA5E"//
				+ "13E7 85C8"//
				+ "0000 0000"//
				+ "0000 0001"//
				, channel);
		assertThrows(CRCMissmatchException.class, () -> protocol.acceptChannel(channel, tiesConversationHandler));
		verify(channel, times(1)).getInput();
		verify(tiesConversationHandler, times(0)).handle(isA(TiesConversation.class));
	}

}
