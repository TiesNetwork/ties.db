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

import com.tiesdb.protocol.v0r0.api.message.DataEntryField;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLHeader;

public class FieldHandler implements TiesDBEBMLHandler<DataEntryField> {

	private static final Logger LOG = LoggerFactory.getLogger(FieldHandler.class);

	public static final FieldHandler INSTANCE = new FieldHandler();

	private FieldHandler() {
	}

	@Override
	public DataEntryField read(TiesDBEBMLParser parser) throws IOException {
		DataEntryField result = new DataEntryField();
		EBMLHeader elementHeader;
		while ((elementHeader = parser.readHeader()) != null) {
			if (FIELD_HASH.equals(elementHeader.getType())) {
				result.setFieldHash(ValueBinaryHandler.INSTANCE.read(parser));
				parser.next();
			} else if (FIELD_NAME.equals(elementHeader.getType())) {
				result.setName(ValueUTF8StringHandler.INSTANCE.read(parser));
				parser.next();
			} else if (FIELD_VALUE.equals(elementHeader.getType())) {
				result.setValue(FieldValueHandler.INSTANCE.read(parser));
				parser.next();
			} else {
				switch (parser.getSettings().getUnexpectedPartStrategy()) {
				case ERROR:
					throw new IOException("Unexpected DataEntryField part " + elementHeader.getType());
				case SKIP:
					LOG.debug("Unexpected DataEntryField part {}", elementHeader.getType());
					parser.skip();
					continue;
				}
			}
		}
		return result;
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(DataEntryField o) throws IOException {
		final Part<TiesDBEBMLFormatter> fieldHash = safe(ValueBinaryHandler.INSTANCE, o.getFieldHash());
		final Part<TiesDBEBMLFormatter> fieldName = safe(ValueUTF8StringHandler.INSTANCE, o.getName());
		final Part<TiesDBEBMLFormatter> fieldValue = safe(FieldValueHandler.INSTANCE, o.getValue());
		return new Part<TiesDBEBMLFormatter>() {

			@Override
			public void write(TiesDBEBMLFormatter formatter) throws IOException {
				if (fieldHash != null) {
					formatter.newHeader(FIELD_HASH, fieldHash.getSize(formatter));
					fieldHash.write(formatter);
				}
				if (fieldName != null) {
					formatter.newHeader(FIELD_NAME, fieldName.getSize(formatter));
					fieldName.write(formatter);
				}
				if (fieldValue != null) {
					formatter.newHeader(FIELD_VALUE, fieldValue.getSize(formatter));
					fieldValue.write(formatter);
				}
			}

			@Override
			public int getSize(TiesDBEBMLFormatter formatter) throws IOException {
				int size = 0;
				if (fieldHash != null) {
					size += formatter.getPartSize(FIELD_HASH, fieldHash.getSize(formatter));
				}
				if (fieldName != null) {
					size += formatter.getPartSize(FIELD_NAME, fieldName.getSize(formatter));
				}
				if (fieldValue != null) {
					size += formatter.getPartSize(FIELD_VALUE, fieldValue.getSize(formatter));
				}
				return size;
			}

		};
	}
}
