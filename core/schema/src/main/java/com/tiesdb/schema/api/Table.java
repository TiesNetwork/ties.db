package com.tiesdb.schema.api;

import java.util.LinkedHashMap;

import com.tiesdb.schema.api.type.Address;
import com.tiesdb.schema.api.type.Id;

public interface Table extends NamedItem {
	boolean hasField(Id id);
	boolean hasField(String name);
	boolean hasTrigger(Id id);
	boolean hasTrigger(String name);
	boolean hasIndex(Id id);
	boolean hasIndex(String name);
	
	LinkedHashMap<Id, Field> getFields();
	LinkedHashMap<Id, Trigger> getTriggers();
	LinkedHashMap<Id, Index> getIndexes();
	LinkedHashMap<Address, Node> getNodes();
	
	Field getField(Id id);
	Field getField(String name);
	Trigger getTrigger(Id id);
	Trigger getTrigger(String name);
	Index getIndex(Id id);
	Index getIndex(String name);
	Node getNode(Address id);
	
	Tablespace getTablespace();
}
