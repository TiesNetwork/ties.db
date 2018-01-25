package com.tiesdb.schema.impl;

import org.web3j.tuples.generated.Tuple3;

import com.tiesdb.schema.api.Field;
import com.tiesdb.schema.api.Table;
import com.tiesdb.schema.api.type.Id;

public class FieldImpl extends NamedItemImpl implements Field {
	Table table;
	String type;
	byte[] def;

	public FieldImpl(TableImpl table, Id id) {
		super(table.schema, id);
		this.table = table;
	}

	@Override
	public String getName() {
		return Utils.send(schema.tiesDB.getTriggerName(table.getId().getValue(), id.getValue()));
	}

	@Override
	public String getType() {
		load();
		return type;
	}

	@Override
	public byte[] getDefault() {
		load();
		return def;
	}

	@Override
	public Table getTable() {
		return table;
	}

	@Override
	protected void load() {
		if(notLoaded()) {
			Tuple3<String, String, byte[]> t = 
					Utils.send(schema.tiesDB.getField(table.getId().getValue(), id.getValue()));
			name = t.getValue1();
			type = t.getValue2();
			def = t.getValue3();
		}
		
	}

}
