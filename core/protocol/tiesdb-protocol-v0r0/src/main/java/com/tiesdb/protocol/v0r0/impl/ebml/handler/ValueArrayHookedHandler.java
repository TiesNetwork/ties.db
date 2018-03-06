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
import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLHeader;
import one.utopic.sparse.ebml.EBMLType;

public class ValueArrayHookedHandler<T> implements TiesDBEBMLHandler<T[]> {

	private static final Logger LOG = LoggerFactory.getLogger(DataEntryHandler.class);

	private final TiesDBEBMLHandler<T> elementHandler;
	private final EBMLType type;
	private final Class<T> clz;

	public ValueArrayHookedHandler(Class<T> clz, EBMLType type, TiesDBEBMLHandler<T> elementHandler) {
		this.clz = clz;
		this.type = type;
		this.elementHandler = elementHandler;
	}

	@Override
	public T[] read(TiesDBEBMLParser parser) throws IOException {
		return read(parser, e -> {
		});
	}

	protected T[] read(TiesDBEBMLParser parser, Consumer<T> elementCousumer) throws IOException {
		LinkedList<T> results = new LinkedList<>();
		EBMLHeader elementHeader;
		while ((elementHeader = parser.readHeader()) != null) {
			if (this.type.equals(elementHeader.getType())) {
				T element = elementHandler.read(parser);
				if (results.add(element)) {
					elementCousumer.accept(element);
				}
				parser.next();
			} else {
				switch (parser.getSettings().getUnexpectedPartStrategy()) {
				case ERROR:
					throw new IOException("Unexpected " + type.getName() + "[] part " + elementHeader.getType());
				case SKIP:
					LOG.debug("Unexpected {}[] part {}", type.getName(), elementHeader.getType());
					parser.skip();
					continue;
				}
			}
		}
		@SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance(clz, 1);
		return results.toArray(result);
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(T[] o) throws IOException {
		@SuppressWarnings("unchecked")
		final Part<TiesDBEBMLFormatter>[] parts = new Part[o.length];
		for (int i = 0; i < o.length; i++) {
			parts[i] = null == o[i] ? null : elementHandler.prepare(o[i]);
		}
		return new Part<TiesDBEBMLFormatter>() {

			@Override
			public void write(TiesDBEBMLFormatter formatter) throws IOException {
				for (int i = 0; i < parts.length; i++) {
					if (null != parts[i]) {
						formatter.newHeader(type, parts[i].getSize(formatter));
						parts[i].write(formatter);
					}
				}
			}

			@Override
			public int getSize(TiesDBEBMLFormatter formatter) throws IOException {
				int size = 0;
				for (int i = 0; i < parts.length; i++) {
					if (null != parts[i]) {
						size += formatter.getPartSize(type, parts[i].getSize(formatter));
					}
				}
				return size;
			}
		};
	}

}
