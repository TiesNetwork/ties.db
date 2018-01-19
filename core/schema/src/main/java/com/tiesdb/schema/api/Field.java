package com.tiesdb.schema.api;

public interface Field extends NamedItem {
	String getType();
	byte[] getDefault();
}
