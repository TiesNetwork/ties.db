package com.tiesdb.schema.api;

import java.util.List;

import com.tiesdb.schema.api.type.Id;

public interface Table extends NamedItem {
	boolean hasField(Id id);
	boolean hasTrigger(Id id);
	
	List<Field> getFields();
	List<Trigger> getTriggers();
	
	Field getField(Id id);
	Trigger getTrigger(Id id);
	
	Tablespace getTablespace();
}
