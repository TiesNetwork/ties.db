package com.tiesdb.protocol.v0.element;

import com.tiesdb.protocol.v0.element.common.TiesElementValueBinary;
import com.tiesdb.protocol.v0.impl.TiesEBMLType;

public class TiesDBEntry extends TiesElementValueBinary implements TiesDBModificationRequest.Part {

	public TiesDBEntry() {
		super(TiesEBMLType.Entry);
	}

	@Override
	public void accept(TiesDBModificationRequest.PartVisitor v) {
		v.visit(this);
	}

}