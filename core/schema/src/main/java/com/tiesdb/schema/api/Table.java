package com.tiesdb.schema.api;

import java.util.LinkedHashMap;

import com.tiesdb.schema.api.type.Address;
import com.tiesdb.schema.api.type.Id;

public interface Table extends NamedItem {
	boolean hasField(Id id);
	boolean hasTrigger(Id id);
	boolean hasIndex(Id id);
	
	LinkedHashMap<Id, Field> getFields();
	LinkedHashMap<Id, Trigger> getTriggers();
	LinkedHashMap<Id, Index> getIndexes();
	LinkedHashMap<Address, Node> getNodes();
	
	Field getField(Id id);
	Trigger getTrigger(Id id);
	Index getIndex(Id id);
	Node getNode(Address id);
	
	Tablespace getTablespace();
}
