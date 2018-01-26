package com.tiesdb.protocol.v0r0.impl.ebml;

import java.util.Objects;

public class TiesDBEBMLParserSettings {

	public static enum UnexpectedPartStrategy {
		SKIP, ERROR
	}

	private UnexpectedPartStrategy unexpectedPartStrategy = UnexpectedPartStrategy.SKIP;

	public UnexpectedPartStrategy getUnexpectedPartStrategy() {
		return unexpectedPartStrategy;
	}

	public void setUnexpectedPartStrategy(UnexpectedPartStrategy unexpectedPartStrategy) {
		this.unexpectedPartStrategy = Objects.requireNonNull(unexpectedPartStrategy);
	}

}
