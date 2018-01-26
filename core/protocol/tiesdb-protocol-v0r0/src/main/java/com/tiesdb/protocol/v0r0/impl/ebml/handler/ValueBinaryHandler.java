package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import java.io.IOException;

import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLFormatter;
import one.utopic.sparse.ebml.reader.EBMLByteReader;
import one.utopic.sparse.ebml.writer.EBMLByteWriter;

public class ValueBinaryHandler implements TiesDBEBMLHandler<byte[]> {

	static final EBMLByteReader READER = new EBMLByteReader();
	static final EBMLByteWriter WRITER = new EBMLByteWriter();

	public static final ValueBinaryHandler INSTANCE = new ValueBinaryHandler();

	private ValueBinaryHandler() {
	}

	@Override
	public byte[] read(TiesDBEBMLParser parser) throws IOException {
		return READER.read(parser);
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(byte[] o) throws IOException {
		final Part<EBMLFormatter> part = null == o ? null : WRITER.prepare(o);
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
