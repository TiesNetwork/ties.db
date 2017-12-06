package com.tiesdb.protocol.v0.exception;

public class PacketSegmentationException extends RuntimeException {

	private static final long serialVersionUID = 3607434390457221186L;

	public PacketSegmentationException(String message, Throwable cause) {
		super(message, cause);
	}

	public PacketSegmentationException(String message) {
		super(message);
	}

	public PacketSegmentationException(Throwable cause) {
		super(cause);
	}

}
