package com.tiesdb.protocol.v0.impl;

import java.util.Objects;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.element.TiesDBRequest;
import com.tiesdb.protocol.v0.element.TiesDBRequestHeader;
import com.tiesdb.protocol.v0.element.TiesDBRequestSignature;
import com.tiesdb.protocol.v0.element.TiesElement;
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
		case Request:
			return new TiesDBRequest();
		case RequestHeader:
			return new TiesDBRequestHeader();
		case RequestSignature:
			return new TiesDBRequestSignature();
		/*
		 * WARNING Do not use default matcher! This switch should return distinct
		 * objects!
		 */
		}
		return null;
	}
}
