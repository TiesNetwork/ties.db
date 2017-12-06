package com.tiesdb.protocol.v0.impl;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.ebml.Element;
import org.ebml.ProtoType;

import com.tiesdb.protocol.api.data.ElementType;
import com.tiesdb.protocol.v0.impl.TiesEBMLExtendedElement.*;

public enum TiesEBMLType implements ElementType {

	Request(MasterElement.class, 0), //
	RequestHeader(MasterElement.class, 1), //
	RequestSignature(BinaryElement.class, 2), //
	;

	static {
		// Preloading TiesEBMLTags
		TiesEBMLTag.values();
	}

	ExtendedProtoType protoType;
	private final Class<Element> type;
	private final int level;

	@SuppressWarnings("unchecked")
	private <T extends Element & TiesEBMLExtendedElement> TiesEBMLType(Class<T> type, int level) {
		Objects.nonNull(type);
		this.type = (Class<Element>) type;
		this.level = level;
	}

	public ExtendedProtoType getProtoType() {
		return protoType;
	}

	@Override
	public String getName() {
		return name();
	}

	public static class ExtendedProtoType extends ProtoType<Element> {

		private final TiesEBMLType tiesEBMLType;

		protected ExtendedProtoType(TiesEBMLType t, byte[] type) {
			super(t.type, t.name(), type, t.level);
			this.tiesEBMLType = t;
		}

		protected TiesEBMLType getTiesEBMLType() {
			return tiesEBMLType;
		}
	}

	static TiesEBMLType getForProtoType(org.ebml.ProtoType<?> proto) {
		return proto instanceof TiesEBMLType.ExtendedProtoType ? ((TiesEBMLType.ExtendedProtoType) proto).getTiesEBMLType() : null;
	}

	static TiesEBMLType getForElement(Element elm) {
		return elm == null ? null : getForProtoType(elm.getElementType());
	}

	static TiesEBMLType getForCode(ByteBuffer typeCode) {
		return getForElement(ProtoType.getInstance(typeCode));
	}
}