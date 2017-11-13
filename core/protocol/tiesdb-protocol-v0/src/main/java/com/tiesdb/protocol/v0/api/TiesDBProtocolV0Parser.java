package com.tiesdb.protocol.v0.api;

import com.tiesdb.protocol.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;

public interface TiesDBProtocolV0Parser {

	Version getMessageVersion() throws TiesDBProtocolException;

}