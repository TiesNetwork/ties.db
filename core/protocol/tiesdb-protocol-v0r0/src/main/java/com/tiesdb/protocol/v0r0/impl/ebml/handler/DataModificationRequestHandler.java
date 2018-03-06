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
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLHeader;

public class DataModificationRequestHandler implements TiesDBEBMLHandler<DataModificationRequest> {

	private static final Logger LOG = LoggerFactory.getLogger(DataModificationRequestHandler.class);

	public static final DataModificationRequestHandler INSTANCE = new DataModificationRequestHandler();

	private DataModificationRequestHandler() {
	}

	@Override
	public DataModificationRequest read(TiesDBEBMLParser parser) throws IOException {
		DataModificationRequest result = new DataModificationRequest();
		EBMLHeader elementHeader;
		while ((elementHeader = parser.readHeader()) != null) {
			if (REQUEST_CONSISTENCY.equals(elementHeader.getType())) {
				result.setConsistencyLevel(RequestConsistencyLevelHandler.INSTANCE.read(parser));
				parser.next();
			} else if (DATA_ENTRY.equals(elementHeader.getType())) {
				result.setDataEntry(DataEntryHandler.INSTANCE.read(parser));
				parser.next();
			} else {
				switch (parser.getSettings().getUnexpectedPartStrategy()) {
				case ERROR:
					throw new IOException("Unexpected DataModificationRequest part " + elementHeader.getType());
				case SKIP:
					LOG.debug("Unexpected DataModificationRequest part {}", elementHeader.getType());
					parser.skip();
					continue;
				}
			}
		}
		return result;
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(DataModificationRequest o) throws IOException {
		final Part<TiesDBEBMLFormatter> consistencyLevel = safe(RequestConsistencyLevelHandler.INSTANCE, o.getConsistencyLevel());
		final Part<TiesDBEBMLFormatter> dataEntry = safe(DataEntryHandler.INSTANCE, o.getDataEntry());
		return new Part<TiesDBEBMLFormatter>() {

			@Override
			public void write(TiesDBEBMLFormatter formatter) throws IOException {
				if (consistencyLevel != null) {
					formatter.newHeader(REQUEST_CONSISTENCY, consistencyLevel.getSize(formatter));
					consistencyLevel.write(formatter);
				}
				if (dataEntry != null) {
					formatter.newHeader(DATA_ENTRY, dataEntry.getSize(formatter));
					dataEntry.write(formatter);
				}
			}

			@Override
			public int getSize(TiesDBEBMLFormatter formatter) throws IOException {
				int size = 0;
				if (consistencyLevel != null) {
					size += formatter.getPartSize(REQUEST_CONSISTENCY, consistencyLevel.getSize(formatter));
				}
				if (dataEntry != null) {
					size += formatter.getPartSize(DATA_ENTRY, dataEntry.getSize(formatter));
				}
				return size;
			}

		};
	}
}
