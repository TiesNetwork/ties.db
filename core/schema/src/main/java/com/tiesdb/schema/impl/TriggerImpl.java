package com.tiesdb.schema.impl;

import com.tiesdb.schema.api.Table;
import com.tiesdb.schema.api.Trigger;
import com.tiesdb.schema.api.type.Id;

public class TriggerImpl extends NamedItemImpl implements Trigger {
	Table table;

	public TriggerImpl(TableImpl table, Id id) {
		super(table.schema, id);
		this.table = table;
	}

	@Override
	public byte[] getPayload() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return Utils.send(schema.tiesDB.getFieldName(table.getTablespace().getId().getValue(), table.getId().getValue(), id.getValue()));
	}

}
