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

import static com.tiesdb.protocol.v0.impl.TiesEBMLType.*;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public enum TiesEBMLTag {

	x1E544945(ModificationRequest), //
	xE0(RequestSignature), //
	xE1(RequestConsistency), //
	xEE(Entry), //
	;

	private static final Map<TiesEBMLType, TiesEBMLTag> idMap;
	static {
		TiesEBMLTag[] values = values();
		WeakHashMap<TiesEBMLType, TiesEBMLTag> map = new WeakHashMap<>();
		for (int i = 0; i < values.length; i++) {
			if (null != map.put(values[i].type, values[i])) {
				throw new InstantiationError("EBML duplicate ID 0" + values[i] + " for " + values[i].type);
			}
		}
		idMap = Collections.unmodifiableMap(map);
	}

	public static TiesEBMLTag getByType(TiesEBMLType type) {
		return idMap.get(type);
	}

	private final byte[] id;
	private final TiesEBMLType type;

	public TiesEBMLType getType() {
		return type;
	}

	private TiesEBMLTag(TiesEBMLType type) {
		Objects.requireNonNull(type);
		this.id = strToId(name().substring(1));
		type.protoType = new TiesEBMLType.ExtendedProtoType(type, id);
		this.type = type;
	}

	private byte[] strToId(String hexString) {
		int len = hexString.length();
		byte[] id = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			id[i / 2] = (byte) (//
			(Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
		}
		return checkEBMLCompatibility(id);
	}

	private byte[] checkEBMLCompatibility(byte[] id) {
		if (id.length == 0) {
			throw new InstantiationError("EBML ID could not be zero length");
		}
		int i = 0;
		byte snip = id[i++];
		int size = 1;
		while (snip == 0) {
			snip = id[i++];
			size += 7;
		}
		size += Integer.numberOfLeadingZeros(0xFF & snip) - 24;
		if (size != id.length) {
			throw new InstantiationError("EBML ID leading zero bits should conform the size in bytes");
		}
		return id;
	}

	static TiesEBMLType.ExtendedProtoType newProtoType(TiesEBMLType t) {
		return new TiesEBMLType.ExtendedProtoType(t, getByType(t).id);
	}

}
