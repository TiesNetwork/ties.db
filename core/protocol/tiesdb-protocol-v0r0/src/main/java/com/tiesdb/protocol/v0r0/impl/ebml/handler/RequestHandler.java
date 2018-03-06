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
package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import static com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLTypeContainer.*;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.v0r0.api.message.DataModificationRequest;
import com.tiesdb.protocol.v0r0.api.message.Request;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLHeader;
import one.utopic.sparse.ebml.EBMLType;

public class RequestHandler implements TiesDBEBMLHandler<Request> {

	private static final Logger LOG = LoggerFactory.getLogger(RequestHandler.class);

	public static final RequestHandler INSTANCE = new RequestHandler();

	private RequestHandler() {
	}

	@Override
	public Request read(TiesDBEBMLParser parser) throws IOException {
		EBMLHeader elementHeader = parser.getHeader();
		if (null == elementHeader) {
			return null;
		}
		if (MODIFICATION_REQUEST.equals(elementHeader.getType())) {
			Request result = DataModificationRequestHandler.INSTANCE.read(parser);
			parser.next();
			return result;
		} else {
			switch (parser.getSettings().getUnexpectedPartStrategy()) {
			case ERROR:
				throw new IOException("Unexpected Request " + elementHeader.getType());
			case SKIP:
				LOG.debug("Unexpected Request {}", elementHeader.getType());
				parser.skip();
				return null;
			}
			throw new IOException("Unexpected behavior for part " + elementHeader.getType());
		}
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(Request o) throws IOException {
		final Part<TiesDBEBMLFormatter> requestPart;
		final EBMLType requestType;
		if (null == o) {
			requestPart = null;
			requestType = null;
		} else if (o instanceof DataModificationRequest) {
			requestPart = DataModificationRequestHandler.INSTANCE.prepare((DataModificationRequest) o);
			requestType = MODIFICATION_REQUEST;
		} else {
			throw new IOException("Unexpected behavior for request " + o.getClass());
		}
		return new Part<TiesDBEBMLFormatter>() {

			@Override
			public void write(TiesDBEBMLFormatter formatter) throws IOException {
				if (requestPart != null && requestType != null) {
					formatter.newHeader(requestType, requestPart.getSize(formatter));
					requestPart.write(formatter);
				}
			}

			@Override
			public int getSize(TiesDBEBMLFormatter formatter) throws IOException {
				int size = 0;
				if (requestPart != null && requestType != null) {
					size += formatter.getPartSize(requestType, requestPart.getSize(formatter));
				}
				return size;
			}

		};
	}

}
