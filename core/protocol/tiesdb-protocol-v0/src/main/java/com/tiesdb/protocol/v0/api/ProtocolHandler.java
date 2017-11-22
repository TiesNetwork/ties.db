package com.tiesdb.protocol.v0.api;

import com.tiesdb.protocol.TiesDBProtocolHandler;
import com.tiesdb.protocol.exception.TiesDBProtocolException;

public interface ProtocolHandler extends TiesDBProtocolHandler {

	void handle(MessageContext context) throws TiesDBProtocolException;

}
