package com.tiesdb.protocol.v0.impl;

import com.tiesdb.protocol.v0.impl.TiesEBMLExtendedElement.*;

public interface TypeVisitor<T> {

	interface Acceptor {
		<T> T accept(TypeVisitor<T> visitor);
	}

	class TypeVisitorCommon<T> implements TypeVisitor<T> {

		protected <E extends org.ebml.Element & TiesEBMLExtendedElement> T defaultValue(E element) {
			throw new IllegalArgumentException("Unexpected " + element);
		}

		@Override
		public T visit(ContainerElement containerElement) {
			return defaultValue(containerElement);
		}

		@Override
		public T visit(ValueElement valueElement) {
			return defaultValue(valueElement);
		}

	}

	T visit(ContainerElement containerElement);

	T visit(ValueElement valueElement);

}
