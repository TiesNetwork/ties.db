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
package com.tiesdb.protocol.v0.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum ConsistencyLevel {
	ONE((byte) 1), //
	QUORUM(Byte.MAX_VALUE), //
	ALL((byte) -1), //
	;

	private static final Map<Byte, ConsistencyLevel> codeMap;
	static {
		ConsistencyLevel[] values = values();
		HashMap<Byte, ConsistencyLevel> map = new HashMap<>();
		for (int i = 0; i < values.length; i++) {
			if (null != map.put(values[i].code, values[i])) {
				throw new InstantiationError("ConsistencyLevel duplicate code " + values[i].code + " for " + values[i]);
			}
		}
		codeMap = Collections.unmodifiableMap(map);
	}

	private final byte code;

	public byte getCode() {
		return code;
	}

	public static ConsistencyLevel getByCode(byte code) {
		return codeMap.get(code);
	}

	private ConsistencyLevel(byte code) {
		this.code = code;
	}
}
