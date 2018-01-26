package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import java.io.IOException;
import java.util.UUID;

import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

public class EthereumAdressHandler implements TiesDBEBMLHandler<UUID> {

	static final ValueSignedLongHandler HANDLER = ValueSignedLongHandler.INSTANCE;

	public static final EthereumAdressHandler INSTANCE = new EthereumAdressHandler();

	private EthereumAdressHandler() {
	}

	@Override
	public UUID read(TiesDBEBMLParser parser) throws IOException {
		return new UUID(HANDLER.read(parser), HANDLER.read(parser));
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(UUID o) throws IOException {
		final Part<TiesDBEBMLFormatter> partMost = HANDLER.prepare(null == o ? null : o.getMostSignificantBits());
		final Part<TiesDBEBMLFormatter> partLeast = HANDLER.prepare(null == o ? null : o.getLeastSignificantBits());
		return new Part<TiesDBEBMLFormatter>() {

			@Override
			public void write(TiesDBEBMLFormatter formatter) throws IOException {
				if (null != partMost) {
					partMost.write(formatter);
				}
				if (null != partLeast) {
					partLeast.write(formatter);
				}
			}

			@Override
			public int getSize(TiesDBEBMLFormatter formatter) throws IOException {
				int size = 0;
				if (null != partMost) {
					size += partMost.getSize(formatter);
				}
				if (null != partLeast) {
					size += partLeast.getSize(formatter);
				}
				return size;
			}
		};
	}

}
