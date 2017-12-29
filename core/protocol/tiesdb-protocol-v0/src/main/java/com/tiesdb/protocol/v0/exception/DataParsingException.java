package com.tiesdb.protocol.v0.exception;

import com.tiesdb.protocol.exception.TiesDBProtocolException;

public class DataParsingException extends RuntimeException {

	private static final long serialVersionUID = 1445466184069662493L;

	public DataParsingException(String message, TiesDBProtocolException cause) {
		super(message, cause);
	}

	public DataParsingException(TiesDBProtocolException cause) {
		super(cause);
	}

}
