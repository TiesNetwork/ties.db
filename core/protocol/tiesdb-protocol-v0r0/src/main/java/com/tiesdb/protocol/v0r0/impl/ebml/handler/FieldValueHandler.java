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

import com.tiesdb.protocol.v0r0.api.message.FieldValue;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLHeader;

public class FieldValueHandler implements TiesDBEBMLHandler<FieldValue> {

	private static final Logger LOG = LoggerFactory.getLogger(FieldValueHandler.class);

	public static final FieldValueHandler INSTANCE = new FieldValueHandler();

	private FieldValueHandler() {
	}

	@Override
	public FieldValue read(TiesDBEBMLParser parser) throws IOException {
		FieldValue result = new FieldValue();
		EBMLHeader elementHeader;
		while ((elementHeader = parser.readHeader()) != null) {
			if (VALUE_TYPE.equals(elementHeader.getType())) {
				result.setType(ValueUTF8StringHandler.INSTANCE.read(parser));
				parser.next();
			} else if (VALUE_DATA.equals(elementHeader.getType())) {
				result.setData(ValueBinaryHandler.INSTANCE.read(parser));
				parser.next();
			} else {
				switch (parser.getSettings().getUnexpectedPartStrategy()) {
				case ERROR:
					throw new IOException("Unexpected FieldValue part " + elementHeader.getType());
				case SKIP:
					LOG.debug("Unexpected FieldValue part {}", elementHeader.getType());
					parser.skip();
					continue;
				}
			}
		}
		return result;
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(FieldValue o) throws IOException {
		final Part<TiesDBEBMLFormatter> fieldType = safe(ValueUTF8StringHandler.INSTANCE, o.getType());
		final Part<TiesDBEBMLFormatter> fieldData = safe(ValueBinaryHandler.INSTANCE, o.getData());
		return new Part<TiesDBEBMLFormatter>() {

			@Override
			public void write(TiesDBEBMLFormatter formatter) throws IOException {
				if (fieldType != null) {
					formatter.newHeader(VALUE_TYPE, fieldType.getSize(formatter));
					fieldType.write(formatter);
				}
				if (fieldData != null) {
					formatter.newHeader(VALUE_DATA, fieldData.getSize(formatter));
					fieldData.write(formatter);
				}
			}

			@Override
			public int getSize(TiesDBEBMLFormatter formatter) throws IOException {
				int size = 0;
				if (fieldType != null) {
					size += formatter.getPartSize(VALUE_TYPE, fieldType.getSize(formatter));
				}
				if (fieldData != null) {
					size += formatter.getPartSize(VALUE_DATA, fieldData.getSize(formatter));
				}
				return size;
			}

		};
	}
}
