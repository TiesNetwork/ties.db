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
package com.tiesdb.protocol.v0;

import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel;
import com.tiesdb.protocol.api.data.ElementReader;
import com.tiesdb.protocol.api.data.ElementWriter;
import com.tiesdb.protocol.api.data.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.TiesConversation;
import com.tiesdb.protocol.v0.api.TiesElement;
import com.tiesdb.protocol.v0.impl.ElementReaderImpl;
import com.tiesdb.protocol.v0.impl.ElementWriterImpl;

public class TiesDBConversationV0 implements TiesConversation {

	private final Version version;
	private final TiesDBProtocolPacketChannel packetChannel;
	private final TiesDBProtocolV0 protocol;

	private ElementReaderImpl reader;
	private ElementWriterImpl writer;

	public TiesDBConversationV0(TiesDBProtocolV0 protocol, TiesDBProtocolPacketChannel packetChannel, Version version) {
		this.protocol = protocol;
		this.packetChannel = packetChannel;
		this.version = version;
	}

	@Override
	public synchronized ElementReader<TiesElement> getReader() throws TiesDBProtocolException {
		return reader != null ? reader : (reader = getProtocol().createReader(packetChannel));
	}

	@Override
	public synchronized ElementWriter<TiesElement> getWriter() {
		return writer != null ? writer : (writer = getProtocol().createWriter(packetChannel));
	}

	@Override
	public Version getVersion() {
		return version;
	}

	@Override
	public TiesDBProtocolV0 getProtocol() {
		return protocol;
	}

}
