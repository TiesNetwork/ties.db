package com.tiesdb.protocol.v0.api;

import com.tiesdb.protocol.api.TiesDBProtocol;
import com.tiesdb.protocol.api.data.ElementReader;
import com.tiesdb.protocol.api.data.ElementWriter;
import com.tiesdb.protocol.api.data.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;

public interface TiesConversation {

	Version getVersion();

	TiesDBProtocol getProtocol();

	ElementReader<TiesElement> getReader() throws TiesDBProtocolException;

	ElementWriter<TiesElement> getWriter() throws TiesDBProtocolException;

}
