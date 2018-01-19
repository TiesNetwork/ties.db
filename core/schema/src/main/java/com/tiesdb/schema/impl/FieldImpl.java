package com.tiesdb.schema.impl;

import com.tiesdb.schema.api.Field;
import com.tiesdb.schema.api.Table;
import com.tiesdb.schema.api.type.Id;

public class FieldImpl extends NamedItemImpl implements Field {
	Table table;

	public FieldImpl(TableImpl table, Id id) {
		super(table.schema, id);
		this.table = table;
	}

	@Override
	public String getName() {
		return Utils.send(schema.tiesDB.getTriggerName(table.getTablespace().getId().getValue(), table.getId().getValue(), id.getValue()));
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getDefault() {
		// TODO Auto-generated method stub
		return null;
	}

}
