package com.tiesdb.schema.impl;

import com.tiesdb.schema.api.type.Id;

public class IdImpl implements Id {
	byte[] value;
	
	public IdImpl(byte[] value) {
		this.value = value;
	}

	@Override
	public byte[] getValue() {
		return value;
	}
}
