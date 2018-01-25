package com.tiesdb.schema.impl;

import com.tiesdb.schema.api.Item;
import com.tiesdb.schema.api.Schema;

public abstract class ItemImpl implements Item {
	SchemaImpl schema;

	public ItemImpl(SchemaImpl schema) {
		this.schema = schema;
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	protected abstract void load();
	
	protected abstract boolean notLoaded();
}
