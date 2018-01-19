package com.tiesdb.schema.api;

import java.util.List;

import com.tiesdb.schema.api.type.Id;

public interface Schema {
	public List<Tablespace> getTablespaceList();
	public List<Node> getNodeList();
	
	public Id idFromName(String name);
	public Id idFromName(String tablespace, String table);
}
