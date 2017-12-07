package com.tiesdb.protocol.v0.element;

import com.tiesdb.protocol.v0.element.common.TiesElementValueBinary;
import com.tiesdb.protocol.v0.impl.TiesEBMLType;

public class TiesDBRequestSignature extends TiesElementValueBinary implements TiesDBRequestHeader.Part {

	public TiesDBRequestSignature() {
		super(TiesEBMLType.RequestHeaderSignature);
	}

	@Override
	public void accept(TiesDBRequestHeader.PartVisitor v) {
		v.visit(this);
	}

}