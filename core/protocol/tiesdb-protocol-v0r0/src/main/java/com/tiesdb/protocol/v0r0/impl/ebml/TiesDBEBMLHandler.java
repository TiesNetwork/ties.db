package com.tiesdb.protocol.v0r0.impl.ebml;

import java.io.IOException;

import one.utopic.sparse.api.Reader;
import one.utopic.sparse.ebml.EBMLWriter;

public interface TiesDBEBMLHandler<T> extends Reader<TiesDBEBMLParser, T>, EBMLWriter<TiesDBEBMLFormatter, T> {

	default <V> Part<TiesDBEBMLFormatter> safe(TiesDBEBMLHandler<? super V> handler, V value) throws IOException {
		if (null == value) {
			return null;
		}
		return handler.prepare(value);
	}
}
