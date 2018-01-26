package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLHeader;
import one.utopic.sparse.ebml.EBMLType;

public class ValueArrayDynamicHandler<T> implements TiesDBEBMLHandler<T[]> {

	private static final Logger LOG = LoggerFactory.getLogger(DataEntryHandler.class);

	private static class HandlerMapping<T> {
		private final Class<? extends T> clas;
		private final EBMLType type;
		private final TiesDBEBMLHandler<? extends T> handler;

		private HandlerMapping(Class<? extends T> clas, EBMLType type, TiesDBEBMLHandler<? extends T> handler) {
			this.clas = Objects.requireNonNull(clas);
			this.type = Objects.requireNonNull(type);
			this.handler = Objects.requireNonNull(handler);
		}
	}

	public static class ValueArrayDynamicHandlerBuilder<T> {

		private final LinkedList<HandlerMapping<? extends T>> handlerMappings = new LinkedList<>();
		private final Class<T> clz;

		public ValueArrayDynamicHandlerBuilder(Class<T> clz) {
			this.clz = clz;
		}

		ValueArrayDynamicHandlerBuilder<T> add(Class<? extends T> clas, EBMLType type, TiesDBEBMLHandler<? extends T> handler) {
			this.handlerMappings.add(new HandlerMapping<T>(clas, type, handler));
			return ValueArrayDynamicHandlerBuilder.this;
		}

		ValueArrayDynamicHandler<T> build() {
			return new ValueArrayDynamicHandler<T>(this.clz, this.handlerMappings);
		}
	}

	private final Class<T> clz;
	private final Map<EBMLType, TiesDBEBMLHandler<? extends T>> handlerTypeMap;
	private final Map<Class<? extends T>, TiesDBEBMLHandler<T>> handlerClassMap;
	private final Map<Class<? extends T>, EBMLType> classTypeMap;

	private ValueArrayDynamicHandler(Class<T> clz, LinkedList<HandlerMapping<? extends T>> handlerMappings) {
		this.clz = clz;
		Map<EBMLType, TiesDBEBMLHandler<? extends T>> handlerTypeMap = new HashMap<>();
		Map<Class<? extends T>, TiesDBEBMLHandler<T>> handlerClassMap = new HashMap<>();
		Map<Class<? extends T>, EBMLType> classTypeMap = new HashMap<>();
		for (HandlerMapping<? extends T> handlerMapping : handlerMappings) {
			handlerTypeMap.put(handlerMapping.type, handlerMapping.handler);
			@SuppressWarnings("unchecked")
			TiesDBEBMLHandler<T> handler = (TiesDBEBMLHandler<T>) handlerMapping.handler;
			handlerClassMap.put(handlerMapping.clas, handler);
			classTypeMap.put(handlerMapping.clas, handlerMapping.type);
		}
		this.handlerTypeMap = Collections.unmodifiableMap(handlerTypeMap);
		this.handlerClassMap = Collections.unmodifiableMap(handlerClassMap);
		this.classTypeMap = Collections.unmodifiableMap(classTypeMap);
	}

	@Override
	public T[] read(TiesDBEBMLParser parser) throws IOException {
		LinkedList<T> results = new LinkedList<>();
		EBMLHeader elementHeader;
		while ((elementHeader = parser.readHeader()) != null) {
			TiesDBEBMLHandler<? extends T> handler = handlerTypeMap.get(elementHeader.getType());
			if (null != handler) {
				results.add(handler.read(parser));
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
		@SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance(clz, 1);
		return results.toArray(result);
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(T[] o) throws IOException {
		@SuppressWarnings("unchecked")
		final Part<TiesDBEBMLFormatter>[] parts = new Part[o.length];
		final EBMLType[] types = new EBMLType[o.length];
		for (int i = 0; i < o.length; i++) {
			if (null == o[i]) {
				continue;
			}
			Class<? extends Object> clas = o[i].getClass();
			TiesDBEBMLHandler<T> elementHandler = handlerClassMap.get(clas);
			EBMLType type = classTypeMap.get(clas);
			if (null == elementHandler || null == type) {
				LOG.debug("Unexpected array element {} in {}", o[i], o);
				continue;
			}
			types[i] = type;
			parts[i] = elementHandler.prepare(o[i]);
		}
		return new Part<TiesDBEBMLFormatter>() {

			private int size = -1;

			@Override
			public void write(TiesDBEBMLFormatter formatter) throws IOException {
				for (int i = 0; i < parts.length; i++) {
					if (null != parts[i]) {
						formatter.newHeader(types[i], parts[i].getSize(formatter));
						parts[i].write(formatter);
						formatter.next();
					}
				}
			}

			@Override
			public int getSize(TiesDBEBMLFormatter formatter) throws IOException {
				if (this.size != -1) {
					return this.size;
				}
				int size = 0;
				for (int i = 0; i < parts.length; i++) {
					if (null != parts[i]) {
						size += formatter.getPartSize(types[i], parts[i].getSize(formatter));
					}
				}
				return this.size = size;
			}
		};
	}

}
