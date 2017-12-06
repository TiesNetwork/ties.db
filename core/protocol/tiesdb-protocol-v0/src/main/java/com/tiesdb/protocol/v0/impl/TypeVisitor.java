package com.tiesdb.protocol.v0.impl;

import com.tiesdb.protocol.v0.impl.TiesEBMLExtendedElement.*;

public interface TypeVisitor<T> {

	interface Acceptor {
		<T> T accept(TypeVisitor<T> visitor);
	}

	T visit(MasterElement masterElement);

	T visit(BinaryElement binaryElement);

	abstract class TypeVisitorCommon<T> implements TypeVisitor<T> {

		@Override
		public T visit(MasterElement masterElement) {
			return defaultValue(masterElement);
		}

		@Override
		public T visit(BinaryElement binaryElement) {
			return defaultValue(binaryElement);
		}

		protected abstract T defaultValue(TiesEBMLExtendedElement element);
	}

}
