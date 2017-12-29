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
