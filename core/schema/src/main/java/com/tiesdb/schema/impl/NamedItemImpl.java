package com.tiesdb.schema.impl;

import com.tiesdb.schema.api.NamedItem;
import com.tiesdb.schema.api.type.Id;

public abstract class NamedItemImpl implements NamedItem {
	SchemaImpl schema;
	Id id;
	
	public NamedItemImpl(SchemaImpl schema, Id id) {
		this.schema = schema;
		this.id = id;
	}

	@Override
	public Id getId() {
		return id;
	}

}
