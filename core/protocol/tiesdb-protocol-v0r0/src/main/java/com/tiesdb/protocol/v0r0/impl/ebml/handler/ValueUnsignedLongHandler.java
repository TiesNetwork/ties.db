package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import java.io.IOException;

import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLFormatter;
import one.utopic.sparse.ebml.reader.EBMLUnsignedLongReader;
import one.utopic.sparse.ebml.writer.EBMLUnsignedLongWriter;

public class ValueUnsignedLongHandler implements TiesDBEBMLHandler<Long> {

	static final EBMLUnsignedLongReader READER = new EBMLUnsignedLongReader();
	static final EBMLUnsignedLongWriter WRITER = new EBMLUnsignedLongWriter();

	public static final ValueUnsignedLongHandler INSTANCE = new ValueUnsignedLongHandler();

	private ValueUnsignedLongHandler() {
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
