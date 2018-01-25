package com.tiesdb.schema.api;

import java.util.LinkedHashMap;

import com.tiesdb.schema.api.type.Id;

public interface Tablespace extends NamedItem{
	boolean hasTable(Id id);
	LinkedHashMap<Id, Table> getTables();
	
	Table getTable(Id id);
	
	Schema getSchema();
}
