package com.tiesdb.schema.api;

import java.util.LinkedHashMap;

import com.tiesdb.schema.api.type.Address;
import com.tiesdb.schema.api.type.Id;

public interface Schema extends Item {
	public LinkedHashMap<Id, Tablespace> getTablespaces();
	public LinkedHashMap<Address, Node> getNodes();
	public Tablespace getTablespace(Id id);
	public Node getNode(Address address);
	
	public Id idFromName(String name);
	public Id idFromName(String tablespace, String table);
}
