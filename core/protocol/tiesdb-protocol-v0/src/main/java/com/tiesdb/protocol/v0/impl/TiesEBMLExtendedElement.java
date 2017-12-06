package com.tiesdb.protocol.v0.impl;

public interface TiesEBMLExtendedElement extends TypeVisitor.Acceptor {
	static class MasterElement extends org.ebml.MasterElement implements TiesEBMLExtendedElement {
		@Override
		public <T> T accept(TypeVisitor<T> visitor) {
			return visitor.visit(this);
		}
	}

	static class BinaryElement extends org.ebml.BinaryElement implements TiesEBMLExtendedElement {
		@Override
		public <T> T accept(TypeVisitor<T> visitor) {
			return visitor.visit(this);
		}
	}
}