package com.tiesdb.protocol.v0.impl;

import java.nio.ByteBuffer;

import com.tiesdb.protocol.v0.impl.TiesEBMLExtendedElement.BinaryElement;

public class TypeValueVisitor<T> extends TypeVisitor.TypeVisitorCommon<T> {

	public static final TypeValueVisitor<byte[]> BINARY = new TypeValueVisitor<byte[]>() {
		@Override
		public byte[] visit(BinaryElement binaryElement) {
			return binaryElement.getDataArray();
		}
	};

	@Override
	protected T defaultValue(TiesEBMLExtendedElement element) {
		throw new IllegalArgumentException("Can't add " + element + " as Value");
	}

	public static class BinaryElementValue extends TypeValueVisitor<BinaryElement> {

		private final byte[] value;

		public BinaryElementValue(byte[] value) {
			this.value = value;
		}

		@Override
		public BinaryElement visit(BinaryElement binaryElement) {
			binaryElement.setData(ByteBuffer.wrap(value));
			return binaryElement;
		}
	}

}