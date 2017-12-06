package com.tiesdb.protocol.v0.impl;

import com.tiesdb.protocol.v0.element.TiesElement;

public abstract class TiesElementValue implements TiesElement {
	protected abstract void setValue(TiesEBMLExtendedElement element);
	protected abstract void getValue(TiesEBMLExtendedElement element);
}
