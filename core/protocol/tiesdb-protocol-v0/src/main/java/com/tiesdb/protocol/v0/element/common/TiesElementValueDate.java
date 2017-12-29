package com.tiesdb.protocol.v0.element.common;

import java.util.Date;

import com.tiesdb.protocol.v0.impl.TiesEBMLType;
import com.tiesdb.protocol.v0.impl.TiesElementValue;

public abstract class TiesElementValueDate extends TiesElementValue<Date> {

	public static final long UNIX_EPOCH_DELAY = 978307200; // 2001/01/01 00:00:00 UTC
	private static final int MIN_SIZE_LENGTH = 8;

	private Date value = new Date(0);

	public TiesElementValueDate(TiesEBMLType type) {
		super(type);
	}

	public Date getValue() {
		return value;
	}

	public void setValue(Date value) {
		this.value = value != null ? value : new Date(0);
	}

	@Override
	protected byte[] getRawValue() {
		final long val = (value.getTime() - UNIX_EPOCH_DELAY) * 1000000000;
		return org.ebml.Element.packInt(val, MIN_SIZE_LENGTH);
	}

	@Override
	protected void setRawValue(byte[] rawValue) {
		long val = 0;
		long tmp = 0;
		val |= ((long) rawValue[0] << (56 - ((8 - rawValue.length) * 8)));
		for (int i = 1; i < rawValue.length; i++) {
			tmp = ((long) rawValue[rawValue.length - i]) << 56;
			tmp >>>= 56 - (8 * (i - 1));
			val |= tmp;
		}
		this.value = new Date(val / 1000000000 + UNIX_EPOCH_DELAY);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		TiesElementValueDate other = (TiesElementValueDate) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}