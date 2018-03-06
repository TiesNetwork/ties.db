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
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.protocol.v0r0.impl;

import static com.tiesdb.protocol.api.Version.VersionComprator.REVISION;

import java.io.IOException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.api.TiesDBProtocol;
import com.tiesdb.protocol.api.TiesDBProtocolHandler;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel;
import com.tiesdb.protocol.api.Version;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.PacketInput;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.api.TiesDBProtocolV0R0Handler;

public class TiesDBProtocolV0R0 implements TiesDBProtocol {

	private static final Logger LOG = LoggerFactory.getLogger(TiesDBProtocolV0R0.class);

	private static final Version VERSION = new Version(0, 0, 1);

	protected final ProtocolHelper protocolHelper;

	public TiesDBProtocolV0R0() {
		this(null);
	}

	public TiesDBProtocolV0R0(ProtocolHelper protocolHelper) {
		this.protocolHelper = protocolHelper != null ? protocolHelper : getDefaultProtocolHelper();
	}

	protected ProtocolHelper getDefaultProtocolHelper() {
		LOG.debug("Using default ProtocolHelper");
		return ProtocolHelper.Default.INSTANCE;
	}

	@Override
	public Version getVersion() {
		return VERSION;
	}

	@Override
	public void createChannel(TiesDBProtocolPacketChannel packetChannel, TiesDBProtocolHandler handler) throws TiesDBProtocolException {
		processChannel(packetChannel, handler, false);
	}

	@Override
	public void acceptChannel(TiesDBProtocolPacketChannel packetChannel, TiesDBProtocolHandler handler) throws TiesDBProtocolException {
		processChannel(packetChannel, handler, true);
	}

	protected void processChannel(TiesDBProtocolPacketChannel packetChannel, TiesDBProtocolHandler handler, boolean checkheader)
			throws TiesDBProtocolException {
		Objects.requireNonNull(packetChannel);
		Objects.requireNonNull(handler);
		if (!TiesDBProtocolV0R0Handler.class.isInstance(handler)) {
			throw new IllegalArgumentException(
					"TiesDBProtocolHandler of class " + handler.getClass() + " should implement an " + TiesDBProtocolV0R0Handler.class);
		}
		if (checkheader) {
			checkHeader(packetChannel);
		} else {
			writeHeader(packetChannel);
		}
		((TiesDBProtocolV0R0Handler) handler).handle(createConversation(packetChannel));
	}

	protected TiesDBConversationV0R0 createConversation(TiesDBProtocolPacketChannel packetChannel) throws TiesDBProtocolException {
		return new TiesDBConversationV0R0(this, packetChannel);
	}

	protected void writeHeader(TiesDBProtocolPacketChannel packetChannel) throws TiesDBProtocolException {
		try {
			protocolHelper.writePacketHeader(VERSION, packetChannel.getOutput());
		} catch (IOException e) {
			throw new TiesDBProtocolException("Can't write header", e);
		}
	}

	@SuppressWarnings("deprecation") // FIXME!!! Remove usage of deprecated methods
	protected void checkHeader(TiesDBProtocolPacketChannel packetChannel) throws TiesDBProtocolException {
		PacketInput packetInput = null;
		try {
			packetInput = packetChannel.getInput();
			packetInput.peekStart();
			Version version = protocolHelper.parsePacketHeader(packetInput);
			if (REVISION.compare(VERSION, version) != 0) {
				throw new TiesDBProtocolException("Version not supported");
			}
			packetInput.peekSkip();
		} catch (IOException e) {
			throw new TiesDBProtocolException("Can't read header", e);
		} finally {
			try {
				if (packetInput != null && packetInput.isPeeking()) {
					packetInput.peekRewind();
				}
			} catch (Throwable th) {
				LOG.error("Can't rewind input.", th);
			}
		}
	}

}
