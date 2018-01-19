package com.tiesdb.schema.api;

import java.util.List;

import com.tiesdb.schema.api.type.Id;

public interface Tablespace extends NamedItem{
	boolean hasTable(Id id);
	List<Table> getTables();
	Table getTable(Id id);
}
