package com.tiesdb.protocol.v0r0.impl.ebml;

import java.io.IOException;

import one.utopic.abio.api.input.Input;
import one.utopic.sparse.ebml.EBMLFilter;
import one.utopic.sparse.ebml.EBMLParser;
import one.utopic.sparse.ebml.EBMLType;

public class TiesDBEBMLParser extends EBMLParser {

	private final TiesDBEBMLParserSettings settings;

	public TiesDBEBMLParser(TiesDBEBMLParserSettings settings, Input input, EBMLType.Context typeContext, EBMLFilter filter)
			throws IOException {
		super(input, typeContext, filter);
		this.settings = settings;
	}

	public TiesDBEBMLParser(TiesDBEBMLParserSettings settings, Input input, EBMLType.Context typeContext) throws IOException {
		this(settings, input, typeContext, null);
	}

	public TiesDBEBMLParser(Input input, EBMLType.Context typeContext, EBMLFilter filter) throws IOException {
		this(null, input, typeContext, filter);
	}

	public TiesDBEBMLParser(Input input, EBMLType.Context typeContext) throws IOException {
		this(null, input, typeContext, null);
	}

	public TiesDBEBMLParserSettings getSettings() {
		return settings;
	}

}
