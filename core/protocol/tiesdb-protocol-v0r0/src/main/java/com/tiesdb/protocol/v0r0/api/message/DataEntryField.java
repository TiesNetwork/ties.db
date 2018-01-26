package com.tiesdb.protocol.v0r0.api.message;

import javax.xml.bind.DatatypeConverter;

public class DataEntryField {

	private String name;
	private FieldValue value;
	private byte[] fieldHash; // hash(name, value.type, value.data)

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FieldValue getValue() {
		return value;
	}

	public void setValue(FieldValue value) {
		this.value = value;
	}

	public byte[] getFieldHash() {
		return fieldHash;
	}

	public void setFieldHash(byte[] fieldHash) {
		this.fieldHash = fieldHash;
	}

	@Override
	public String toString() {
		return "DataEntryField [name=" + name + ", value=" + value + ", fieldHash="
				+ (fieldHash == null ? "null" : DatatypeConverter.printHexBinary(fieldHash)) + "]";
	}

}
