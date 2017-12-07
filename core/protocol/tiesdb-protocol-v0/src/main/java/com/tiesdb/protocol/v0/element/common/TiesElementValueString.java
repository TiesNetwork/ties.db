package com.tiesdb.protocol.v0.element.common;

import java.nio.charset.Charset;

import com.tiesdb.protocol.v0.impl.TiesEBMLType;
import com.tiesdb.protocol.v0.impl.TiesElementValue;

public abstract class TiesElementValueString extends TiesElementValue<String> {

	private final Charset charset;
	private String value = "";

	public TiesElementValueString(TiesEBMLType type) {
		this(type, Charset.defaultCharset());
	}

	public TiesElementValueString(TiesEBMLType type, Charset charset) {
		super(type);
		this.charset = charset;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	protected byte[] getRawValue() {
		return value.getBytes(charset);
	}

	@Override
	protected void setRawValue(byte[] rawValue) {
		this.value = new String(rawValue, charset);
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
		TiesElementValueString other = (TiesElementValueString) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}