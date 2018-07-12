package com.tiesdb.schema.impl;

import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;

import com.tiesdb.schema.api.Table;
import com.tiesdb.schema.api.Trigger;
import com.tiesdb.schema.api.type.Id;

public class TriggerImpl extends NamedItemImpl implements Trigger {
	Table table;
	byte[] payload;

	public TriggerImpl(TableImpl table, Id id) {
		super(table.schema, id);
		this.table = table;
	}

	@Override
	public byte[] getPayload() {
		load();
		return payload;
	}

	@Override
	public Table getTable() {
		return table;
	}

	@Override
	protected void load() {
		if(notLoaded()) {
			Tuple2<String, byte[]> t = 
					Utils.send(schema.tiesDB.getTrigger(table.getId().getValue(), id.getValue()));
			name = t.getValue1();
			payload = t.getValue2();
		}
	}

}
