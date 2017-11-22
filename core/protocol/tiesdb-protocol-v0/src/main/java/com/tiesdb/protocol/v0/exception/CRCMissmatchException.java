package com.tiesdb.protocol.v0.exception;

import com.tiesdb.protocol.exception.TiesDBProtocolException;

public class CRCMissmatchException extends TiesDBProtocolException {

	private static final long serialVersionUID = 909803823041430521L;

	public CRCMissmatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public CRCMissmatchException(String message) {
		super(message);
	}

	public CRCMissmatchException(Throwable cause) {
		super(cause);
	}

}
