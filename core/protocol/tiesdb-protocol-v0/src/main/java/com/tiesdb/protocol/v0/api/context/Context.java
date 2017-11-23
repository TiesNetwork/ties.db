package com.tiesdb.protocol.v0.api.context;

import com.tiesdb.protocol.exception.TiesDBProtocolException;

public interface Context<T extends Enum<T>> {

	boolean isClosed();

	Context<?> blockedBy();

	boolean isWaiting();

	T next() throws TiesDBProtocolException;

	void parse() throws TiesDBProtocolException;

	void skip() throws TiesDBProtocolException;
}
