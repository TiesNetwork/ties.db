package com.tiesdb.protocol.v0.element.common;

import com.tiesdb.protocol.v0.impl.TiesEBMLType;
import com.tiesdb.protocol.v0.impl.TiesElementValue;

public abstract class TiesElementValueSigned extends TiesElementValue<Long> {

	private long value = 0;

	public TiesElementValueSigned(TiesEBMLType type) {
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
		return org.ebml.Element.packInt(value);
	}

	@Override
	protected void setRawValue(byte[] rawValue) {
		long l = 0;
		long tmp = 0;
		l |= ((long) rawValue[0] << (56 - ((8 - rawValue.length) * 8)));
		for (int i = 1; i < rawValue.length; i++) {
			tmp = ((long) rawValue[rawValue.length - i]) << 56;
			tmp >>>= 56 - (8 * (i - 1));
			l |= tmp;
		}
		this.value = l;
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
		TiesElementValueSigned other = (TiesElementValueSigned) obj;
		if (value != other.value)
			return false;
		return true;
	}

}