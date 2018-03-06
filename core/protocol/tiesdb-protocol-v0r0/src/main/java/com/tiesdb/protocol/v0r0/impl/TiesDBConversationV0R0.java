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

import com.tiesdb.lib.crypto.ecc.signature.ECKey;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.PacketOutput;
import com.tiesdb.protocol.api.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.api.message.Request;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatterSettings;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParserSettings;
import com.tiesdb.protocol.v0r0.impl.ebml.handler.RequestHandler;


import static com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLTypeContainer.ROOT_CTX;

import java.io.IOException;

public class TiesDBConversationV0R0 {

	private final TiesDBProtocolPacketChannel packetChannel;
	private final TiesDBProtocolV0R0 protocol;
	private TiesDBEBMLParserSettings parserSettings;
	private TiesDBEBMLFormatterSettings formatterSettings;

	public TiesDBConversationV0R0(TiesDBProtocolV0R0 protocol, TiesDBProtocolPacketChannel packetChannel) {
		this.protocol = protocol;
		this.packetChannel = packetChannel;
	}

	public Version getVersion() {
		return protocol.getVersion();
	}

	public TiesDBEBMLParserSettings getParserSettings() {
		return parserSettings;
	}

	public TiesDBEBMLFormatterSettings getFormatterSettings() {
		return formatterSettings;
	}

	public void setParserSettings(TiesDBEBMLParserSettings parserSettings) {
		this.parserSettings = parserSettings;
	}

	public Request recieve() throws TiesDBProtocolException {
		try {
			TiesDBEBMLParser parser = new TiesDBEBMLParser(parserSettings, packetChannel.getInput(), ROOT_CTX);
			Request request = RequestHandler.INSTANCE.read(parser);
			return request;
		} catch (IOException e) {
			throw new TiesDBProtocolException("Can't recieve request", e);
		}
	}

	public void send(Request request, ECKey key) throws TiesDBProtocolException {
		PacketOutput output = packetChannel.getOutput();
		TiesDBEBMLFormatter formatter = new TiesDBEBMLFormatter(formatterSettings, key, output);
		try {
			RequestHandler.INSTANCE.prepare(request).write(formatter);
		} catch (IOException e) {
			throw new TiesDBProtocolException("Can't send request: " + request, e);
		}
	}

}
