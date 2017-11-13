package com.tiesdb.protocol.exception;

public class TiesDBProtocolException extends Exception {

	private static final long serialVersionUID = -8447132076835783648L;

	public TiesDBProtocolException(String message) {
		super(message);
	}

	public TiesDBProtocolException(Throwable cause) {
		super(cause);
	}

	public TiesDBProtocolException(String message, Throwable cause) {
		super(message, cause);
	}

}
