package com.tiesdb.protocol.v0r0.impl.ebml;

import java.io.IOException;

public class TiesDBProtocolHandlerException extends IOException {

	private static final long serialVersionUID = -5230578577211443251L;

	public TiesDBProtocolHandlerException() {
		super();
	}

	public TiesDBProtocolHandlerException(String message, Throwable cause) {
		super(message, cause);
	}

	public TiesDBProtocolHandlerException(String message) {
		super(message);
	}

	public TiesDBProtocolHandlerException(Throwable cause) {
		super(cause);
	}

}
