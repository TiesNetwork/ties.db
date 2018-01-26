package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import java.io.IOException;
import java.nio.charset.Charset;

import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLFormatter;
import one.utopic.sparse.ebml.reader.EBMLStringReader;
import one.utopic.sparse.ebml.writer.EBMLStringWriter;

public class ValueUTF8StringHandler implements TiesDBEBMLHandler<String> {

	private static final String CHARSET_NAME = "UTF8";

	static final EBMLStringReader READER = new EBMLStringReader(Charset.forName(CHARSET_NAME), ValueBinaryHandler.READER);
	static final EBMLStringWriter WRITER = new EBMLStringWriter(Charset.forName(CHARSET_NAME), ValueBinaryHandler.WRITER);

	public static final ValueUTF8StringHandler INSTANCE = new ValueUTF8StringHandler();

	private ValueUTF8StringHandler() {
	}

	@Override
	public String read(TiesDBEBMLParser parser) throws IOException {
		return READER.read(parser);
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(String o) throws IOException {
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
