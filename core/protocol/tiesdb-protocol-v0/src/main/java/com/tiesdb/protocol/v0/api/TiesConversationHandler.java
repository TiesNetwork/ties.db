package com.tiesdb.protocol.v0.api;

import com.tiesdb.protocol.api.TiesDBProtocolHandler;
import com.tiesdb.protocol.exception.TiesDBProtocolException;

public interface TiesConversationHandler extends TiesDBProtocolHandler {

	void handle(TiesConversation conv) throws TiesDBProtocolException;

}
