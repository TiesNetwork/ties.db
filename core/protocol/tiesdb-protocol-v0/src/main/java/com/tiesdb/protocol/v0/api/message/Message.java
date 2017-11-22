package com.tiesdb.protocol.v0.api.message;

import com.tiesdb.protocol.exception.TiesDBProtocolException;

public interface Message {

	MessageHeader getHeader() throws TiesDBProtocolException;

}
