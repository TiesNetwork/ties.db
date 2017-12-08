package com.tiesdb.protocol.v0.impl;

import java.util.Objects;

import org.ebml.Element;
import org.ebml.ProtoType;

import com.tiesdb.protocol.api.data.ElementType;
import static com.tiesdb.protocol.v0.impl.TiesEBMLExtendedElement.*;

public enum TiesEBMLType implements ElementType {

	ModificationRequest(CONTAINER, 0), //
	RequestConsistency(VALUE, 1), //
	RequestSignature(VALUE, 1), //
	Entry(VALUE, 1), //
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

}