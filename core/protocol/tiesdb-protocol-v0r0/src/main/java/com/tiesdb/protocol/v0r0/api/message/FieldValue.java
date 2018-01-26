package com.tiesdb.protocol.v0r0.api.message;

public class FieldValue {

	private String type;
	private byte[] data;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "FieldValue [type=" + type + ", data.length=" + (null == data ? "null" : data.length) + "]";
	}

}
