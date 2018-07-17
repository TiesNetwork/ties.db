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
