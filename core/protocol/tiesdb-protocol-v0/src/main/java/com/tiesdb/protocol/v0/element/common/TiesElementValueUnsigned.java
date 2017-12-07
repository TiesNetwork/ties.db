package com.tiesdb.protocol.v0.element.common;

import java.nio.ByteBuffer;

import org.ebml.EBMLReader;

import com.tiesdb.protocol.v0.impl.TiesEBMLType;
import com.tiesdb.protocol.v0.impl.TiesElementValue;

public abstract class TiesElementValueUnsigned extends TiesElementValue<Long> {

	private long value = 0;

	public TiesElementValueUnsigned(TiesEBMLType type) {
		super(type);
	}

	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}

	@Override
	protected byte[] getRawValue() {
		return org.ebml.Element.packIntUnsigned(value);
	}

	@Override
	protected void setRawValue(byte[] rawValue) {
		this.value = EBMLReader.parseEBMLCode(ByteBuffer.wrap(rawValue));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (value ^ (value >>> 32));
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
		TiesElementValueUnsigned other = (TiesElementValueUnsigned) obj;
		if (value != other.value)
			return false;
		return true;
	}

}