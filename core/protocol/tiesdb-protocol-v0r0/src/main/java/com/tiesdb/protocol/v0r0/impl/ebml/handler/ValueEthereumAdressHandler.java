package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import java.io.IOException;

import com.tiesdb.protocol.v0r0.impl.EthereumAddress;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

public class ValueEthereumAdressHandler implements TiesDBEBMLHandler<EthereumAddress> {

	static final ValueBinaryHandler HANDLER = ValueBinaryHandler.INSTANCE;

	public static final ValueEthereumAdressHandler INSTANCE = new ValueEthereumAdressHandler();

	private ValueEthereumAdressHandler() {
	}

	@Override
	public EthereumAddress read(TiesDBEBMLParser parser) throws IOException {
		return new EthereumAddress(HANDLER.read(parser));
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(EthereumAddress o) throws IOException {
		return HANDLER.prepare(null == o ? null : o.getBytes());
	}

}
