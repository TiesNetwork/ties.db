package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import java.io.IOException;

import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLFormatter;
import one.utopic.sparse.ebml.reader.EBMLSignedLongReader;
import one.utopic.sparse.ebml.writer.EBMLSignedLongWriter;

public class ValueSignedLongHandler implements TiesDBEBMLHandler<Long> {

	static final EBMLSignedLongReader READER = new EBMLSignedLongReader();
	static final EBMLSignedLongWriter WRITER = new EBMLSignedLongWriter();

	public static final ValueSignedLongHandler INSTANCE = new ValueSignedLongHandler();

	private ValueSignedLongHandler() {
	}

	@Override
	public Long read(TiesDBEBMLParser parser) throws IOException {
		return READER.read(parser);
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(Long o) throws IOException {
		final Part<EBMLFormatter> part = null == o ? null : WRITER.prepare(o.longValue());
		return new Part<TiesDBEBMLFormatter>() {

			@Override
			public void write(TiesDBEBMLFormatter formatter) throws IOException {
				if (null != part) {
					part.write(formatter);
				}
			}

			@Override
			public int getSize(TiesDBEBMLFormatter formatter) throws IOException {
				if (null != part) {
					return part.getSize(formatter);
				}
				return 0;
			}
		};
	}

}
