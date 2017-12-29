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
 * with Ties.DB project.If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.protocol.v0.impl;

import java.util.Objects;

import org.ebml.Element;
import org.ebml.ProtoType;

import com.tiesdb.protocol.api.data.ElementType;
import static com.tiesdb.protocol.v0.impl.TiesEBMLExtendedElement.*;

public enum TiesEBMLType implements ElementType {

	ModificationRequest(CONTAINER, 0), //
	RequestConsistency(VALUE, 1), //
	RequestSignature(VALUE, 1), //
	Entry(VALUE, 1), //
	;

	static {
		// Preloading TiesEBMLTags
		TiesEBMLTag.values();
	}

	ExtendedProtoType protoType;
	private final Class<Element> type;
	private final int level;

	@SuppressWarnings("unchecked")
	private <T extends Element> TiesEBMLType(Class<T> type, int level) {
		Objects.nonNull(type);
		this.type = (Class<Element>) type;
		this.level = level;
	}

	public ExtendedProtoType getProtoType() {
		return protoType;
	}

	@Override
	public String getName() {
		return name();
	}

	public static class ExtendedProtoType extends ProtoType<Element> {

		private final TiesEBMLType tiesEBMLType;

		protected ExtendedProtoType(TiesEBMLType t, byte[] type) {
			super(t.type, t.name(), type, t.level);
			this.tiesEBMLType = t;
		}

		protected TiesEBMLType getTiesEBMLType() {
			return tiesEBMLType;
		}
	}

}