package com.tiesdb.protocol.v0.impl;

public interface TiesEBMLExtendedElement extends TypeVisitor.Acceptor {

	public static final Class<ContainerElement> CONTAINER = ContainerElement.class;
	public static final Class<ValueElement> VALUE = ValueElement.class;

	static class ContainerElement extends org.ebml.MasterElement implements TiesEBMLExtendedElement {
		@Override
		public <T> T accept(TypeVisitor<T> visitor) {
			return visitor.visit(this);
		}
	}

	static class ValueElement extends org.ebml.BinaryElement implements TiesEBMLExtendedElement {
		@Override
		public <T> T accept(TypeVisitor<T> visitor) {
			return visitor.visit(this);
		}
	}
}