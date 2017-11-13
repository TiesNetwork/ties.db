package com.tiesdb.protocol.v0.api;

import com.tiesdb.protocol.TiesDBProtocolHandler;
import com.tiesdb.protocol.exception.TiesDBProtocolException;

public interface TiesDBProtocolV0Handler extends TiesDBProtocolHandler {

	void handle(TiesDBProtocolV0MessageContext context) throws TiesDBProtocolException;

}
