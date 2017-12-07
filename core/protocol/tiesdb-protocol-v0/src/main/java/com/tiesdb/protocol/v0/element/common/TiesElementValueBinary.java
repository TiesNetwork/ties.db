package com.tiesdb.protocol.v0.element.common;

import java.util.Arrays;

import com.tiesdb.protocol.v0.impl.TiesEBMLType;
import com.tiesdb.protocol.v0.impl.TiesElementValue;

public abstract class TiesElementValueBinary extends TiesElementValue<byte[]> {

	private byte[] value = new byte[] {};

	public TiesElementValueBinary(TiesEBMLType type) {
		super(type);
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value != null ? value : new byte[] {};
	}

	@Override
	protected byte[] getRawValue() {
		return value;
	}

	@Override
	protected void setRawValue(byte[] rawValue) {
		this.value = rawValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(value);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TiesElementValueBinary other = (TiesElementValueBinary) obj;
		if (!Arrays.equals(value, other.value))
			return false;
		return true;
	}

}