package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLHeader;
import one.utopic.sparse.ebml.EBMLType;

public class ValueArrayHandler<T> implements TiesDBEBMLHandler<T[]> {

	private static final Logger LOG = LoggerFactory.getLogger(DataEntryHandler.class);

	private final TiesDBEBMLHandler<T> elementHandler;
	private final EBMLType type;
	private final Class<T> clz;

	public ValueArrayHandler(Class<T> clz, EBMLType type, TiesDBEBMLHandler<T> elementHandler) {
		this.clz = clz;
		this.type = type;
		this.elementHandler = elementHandler;
	}

	@Override
	public T[] read(TiesDBEBMLParser parser) throws IOException {
		LinkedList<T> results = new LinkedList<>();
		EBMLHeader elementHeader;
		while ((elementHeader = parser.readHeader()) != null) {
			if (this.type.equals(elementHeader.getType())) {
				results.add(elementHandler.read(parser));
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
						formatter.next();
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
