package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import java.io.IOException;
import java.math.BigInteger;

import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

public class ValueBigIntegerHandler implements TiesDBEBMLHandler<BigInteger> {

	static final ValueBinaryHandler HANDLER = ValueBinaryHandler.INSTANCE;

	public static final ValueBigIntegerHandler INSTANCE = new ValueBigIntegerHandler();

	private ValueBigIntegerHandler() {
	}

	@Override
	public BigInteger read(TiesDBEBMLParser parser) throws IOException {
		return new BigInteger(HANDLER.read(parser));
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(BigInteger o) throws IOException {
		return HANDLER.prepare(null == o ? null : o.toByteArray());
	}

}
