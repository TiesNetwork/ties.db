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

import java.nio.ByteBuffer;

import org.ebml.Element;
import org.ebml.ProtoType;

public abstract class ElementHelper {

	protected static TiesEBMLExtendedElement asExtended(Element elm) {
		return elm instanceof TiesEBMLExtendedElement ? ((TiesEBMLExtendedElement) elm) : null;
	}

	protected static TiesEBMLType getForProtoType(org.ebml.ProtoType<?> proto) {
		return proto instanceof TiesEBMLType.ExtendedProtoType ? ((TiesEBMLType.ExtendedProtoType) proto).getTiesEBMLType() : null;
	}

	protected static TiesEBMLType getForElement(Element elm) {
		return elm == null ? null : getForProtoType(elm.getElementType());
	}

	protected static TiesEBMLType getForCode(ByteBuffer typeCode) {
		return getForElement(ProtoType.getInstance(typeCode));
	}
}
