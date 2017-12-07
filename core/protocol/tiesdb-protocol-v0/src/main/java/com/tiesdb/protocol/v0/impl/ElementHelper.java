package com.tiesdb.protocol.v0.impl;

import java.nio.ByteBuffer;

import org.ebml.Element;
import org.ebml.ProtoType;

public abstract class ElementHelper {

	protected static TiesEBMLExtendedElement asExtended(Element elm) {
		return elm instanceof TiesEBMLExtendedElement ? ((TiesEBMLExtendedElement) elm) : null;
	}

	protected static TiesEBMLType getForProtoType(org.ebml.ProtoType<?> proto) {
		return proto instanceof TiesEBMLType.ExtendedProtoType ? ((TiesEBMLType.ExtendedProtoType) proto).getTiesEBMLType() : null;
	}

	protected static TiesEBMLType getForElement(Element elm) {
		return elm == null ? null : getForProtoType(elm.getElementType());
	}

	protected static TiesEBMLType getForCode(ByteBuffer typeCode) {
		return getForElement(ProtoType.getInstance(typeCode));
	}
}
