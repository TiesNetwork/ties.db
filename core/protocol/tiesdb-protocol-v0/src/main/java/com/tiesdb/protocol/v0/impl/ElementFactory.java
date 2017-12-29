package com.tiesdb.protocol.v0.impl;

import java.util.Objects;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.TiesElement;
import com.tiesdb.protocol.v0.element.TiesDBEntry;
import com.tiesdb.protocol.v0.element.TiesDBModificationRequest;
import com.tiesdb.protocol.v0.element.TiesDBRequestSignature;
import com.tiesdb.protocol.v0.element.TiesDBRequestConsistency;
import com.tiesdb.protocol.v0.util.DefaultHelper;

public class ElementFactory {

	public static final class Default {
		public static final ElementFactory INSTANCE = new ElementFactory();
		static {
			DefaultHelper.trace("Loaded default {}", INSTANCE);
		}
	}

	public TiesElement getElement(TiesEBMLType type) throws TiesDBProtocolException {
		Objects.requireNonNull(type);
		switch (type) {
		case ModificationRequest:
			return new TiesDBModificationRequest();
		case RequestSignature:
			return new TiesDBRequestSignature();
		case RequestConsistency:
			return new TiesDBRequestConsistency();
		case Entry:
			return new TiesDBEntry();
		/*
		 * WARNING Do not use default matcher! This switch should return distinct
		 * objects!
		 */
		}
		throw new TiesDBProtocolException("Can't create element of type " + type);
	}
}
