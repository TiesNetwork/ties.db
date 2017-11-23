package com.tiesdb.protocol.v0.api.context;

public interface EntryContext extends Context<EntryContext.Part> {

	enum Part {
		SIGNATURE, HEADER, FIELDS, CHEQUES
	}

	MessageContext getMessageContext();
}
