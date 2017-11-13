package com.tiesdb.protocol.v0.impl.util;

import com.tiesdb.protocol.exception.TiesDBProtocolException;

public class MessagePart<E extends TiesDBProtocolException, T> extends LazyPart<E, T> {

	public MessagePart(CheckedSupplier<E, T> supplier) {
		super(supplier);
	}

}
