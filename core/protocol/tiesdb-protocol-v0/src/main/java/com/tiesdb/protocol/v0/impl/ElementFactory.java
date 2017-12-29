/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
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
