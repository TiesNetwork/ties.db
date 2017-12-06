package com.tiesdb.protocol.v0.element;

import java.util.Arrays;

import com.tiesdb.protocol.v0.impl.TiesEBMLExtendedElement;
import com.tiesdb.protocol.v0.impl.TiesEBMLType;
import com.tiesdb.protocol.v0.impl.TiesElementValue;
import com.tiesdb.protocol.v0.impl.TypeValueVisitor;

public class TiesDBRequestSignature extends TiesElementValue implements TiesDBRequestHeader.Part {

	private byte[] value;

	@Override
	public TiesEBMLType getType() {
		return TiesEBMLType.RequestSignature;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	@Override
	protected void setValue(TiesEBMLExtendedElement element) {
		setValue(element.accept(TypeValueVisitor.BINARY));
	}

	@Override
	protected void getValue(TiesEBMLExtendedElement element) {
		element.accept(new TypeValueVisitor.BinaryElementValue(value));
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
		TiesDBRequestSignature other = (TiesDBRequestSignature) obj;
		if (!Arrays.equals(value, other.value))
			return false;
		return true;
	}

}