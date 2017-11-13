package com.tiesdb.protocol.v0.impl;

import com.tiesdb.protocol.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.TiesDBProtocolV0MessageContext;
import com.tiesdb.protocol.v0.api.TiesDBProtocolV0Parser;
import com.tiesdb.protocol.v0.impl.util.CheckedSupplier;
import com.tiesdb.protocol.v0.impl.util.MessagePart;

public class TiesDBProtocolV0MessageContextImpl extends LazyMessageContextImpl
		implements TiesDBProtocolV0MessageContext {

	private final MessagePart<TiesDBProtocolException, Version> messageVersion = part(parser::getMessageVersion);

	protected TiesDBProtocolV0MessageContextImpl(TiesDBProtocolV0Parser parser) {
		super(parser);
	}

	@Override
	public Version getMessageVersion() throws TiesDBProtocolException {
		return messageVersion.get();
	}
}

class LazyMessageContextImpl {

	protected final TiesDBProtocolV0Parser parser;

	protected <E extends TiesDBProtocolException, T> MessagePart<E, T> part(CheckedSupplier<E, T> supplier) {
		return new MessagePart<>(supplier);
	}

	protected LazyMessageContextImpl(TiesDBProtocolV0Parser parser) {
		this.parser = parser;
	}
}