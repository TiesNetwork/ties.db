package com.tiesdb.schema.api;

import java.util.LinkedHashMap;

import com.tiesdb.schema.api.type.Id;

public interface Index extends NamedItem {
	byte getType();
	LinkedHashMap<Id, Field> getFields();
	
	Table getTable();
}
