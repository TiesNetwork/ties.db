package com.tiesdb.schema.impl;

import com.tiesdb.schema.api.NamedItem;
import com.tiesdb.schema.api.type.Id;

public abstract class NamedItemImpl extends ItemImpl implements NamedItem {
	Id id;
	String name;
	
	public NamedItemImpl(SchemaImpl schema, Id id) {
		super(schema);
		this.id = id;
	}

	@Override
	public Id getId() {
		return id;
	}
	
	@Override
	public String getName() {
		load();
		return name;
	}
	
	protected boolean notLoaded(){
		return name == null;
	}	
}
