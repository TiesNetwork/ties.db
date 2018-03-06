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

import java.io.IOException;
import java.util.UUID;

import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

public class ValueUUIDHandler implements TiesDBEBMLHandler<UUID> {

	static final ValueSignedLongHandler HANDLER = ValueSignedLongHandler.INSTANCE;

	public static final ValueUUIDHandler INSTANCE = new ValueUUIDHandler();

	private ValueUUIDHandler() {
	}

	@Override
	public UUID read(TiesDBEBMLParser parser) throws IOException {
		return new UUID(HANDLER.read(parser), HANDLER.read(parser));
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(UUID o) throws IOException {
		final Part<TiesDBEBMLFormatter> partMost = HANDLER.prepare(null == o ? null : o.getMostSignificantBits());
		final Part<TiesDBEBMLFormatter> partLeast = HANDLER.prepare(null == o ? null : o.getLeastSignificantBits());
		return new Part<TiesDBEBMLFormatter>() {

			@Override
			public void write(TiesDBEBMLFormatter formatter) throws IOException {
				if (null != partMost) {
					partMost.write(formatter);
				}
				if (null != partLeast) {
					partLeast.write(formatter);
				}
			}

			@Override
			public int getSize(TiesDBEBMLFormatter formatter) throws IOException {
				int size = 0;
				if (null != partMost) {
					size += partMost.getSize(formatter);
				}
				if (null != partLeast) {
					size += partLeast.getSize(formatter);
				}
				return size;
			}
		};
	}

}
