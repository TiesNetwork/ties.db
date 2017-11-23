package com.tiesdb.protocol.v0.api;

import com.tiesdb.protocol.api.TiesDBProtocolHandler;
import com.tiesdb.protocol.exception.TiesDBProtocolException;

public interface Handler extends TiesDBProtocolHandler {

	void handle(Conversation conv) throws TiesDBProtocolException;

}
