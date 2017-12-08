package com.tiesdb.protocol.v0.element;

import com.tiesdb.protocol.v0.element.common.TiesElementValueBinary;
import com.tiesdb.protocol.v0.impl.TiesEBMLType;

public class TiesDBRequestSignature extends TiesElementValueBinary implements TiesDBBaseRequest.Part {

	public TiesDBRequestSignature() {
		super(TiesEBMLType.RequestSignature);
	}

	@Override
	public void accept(TiesDBBaseRequest.PartVisitor v) {
		v.visit(this);
	}

}