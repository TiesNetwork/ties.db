package com.tiesdb.schema.impl;

import com.tiesdb.schema.api.Range;

public class RangeImpl implements Range {
	int divider;
	int remainder;
	
	public RangeImpl(int divider, int remainder) {
		this.divider = divider;
		this.remainder = remainder;
	}

	@Override
	public int getDivider() {
		return divider;
	}

	@Override
	public int getRemainder() {
		return remainder;
	}

}
