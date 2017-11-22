package com.tiesdb.protocol.v0.api;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.message.Message;

public interface MessageContext {

	Message getMessage() throws TiesDBProtocolException;

}
