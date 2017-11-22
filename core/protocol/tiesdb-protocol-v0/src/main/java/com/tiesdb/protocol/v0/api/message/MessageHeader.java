package com.tiesdb.protocol.v0.api.message;

import com.tiesdb.protocol.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;

public interface MessageHeader {

	Version getVersion() throws TiesDBProtocolException;

}
