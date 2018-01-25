package com.tiesdb.schema.impl;

import java.util.Arrays;

import com.tiesdb.schema.api.type.Id;

public class IdImpl implements Id {
	byte[] value;
	private int hashCode = 0;
	
	public IdImpl(byte[] value) {
		this.value = value;
	}

	@Override
	public byte[] getValue() {
		return value;
	}
	
	private int bytesToInt(byte[] input, int offset) {
		int up = Math.min(input.length, offset + 4);
		
		int out = 0;
		for(int i=0; i<4; ++i) {
			if(i + offset >= up)
				break;
			out |= input[i + offset] << 8*i;
		}
		return out;
	}
	
	@Override
	public int hashCode() {
		if(hashCode != 0)
			return hashCode;
		
		for(int i=0; i<value.length; i += 4) {
			int code = bytesToInt(value, i);
			hashCode ^= code;
		}
		
		if(hashCode == 0)
			hashCode = -1;
		
		return hashCode;
	}
	
	@Override
	public boolean equals(Object other) {
		Id id = (Id)other;
		return Arrays.equals(this.value, id.getValue());
	}
}
