package com.tiesdb.schema.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.tiesdb.schema.api.Field;
import com.tiesdb.schema.api.Table;
import com.tiesdb.schema.api.Tablespace;
import com.tiesdb.schema.api.Trigger;
import com.tiesdb.schema.api.type.Id;

public class TableImpl extends NamedItemImpl implements Table {
	Tablespace ts;

	public TableImpl(Tablespace ts, Id id) {
		super(((TablespaceImpl)ts).schema, id);
		this.ts = ts; 
	}

	@Override
	public boolean hasField(Id id) {
		return Utils.send(schema.tiesDB.hasField(ts.getId().getValue(), id.getValue(), id.getValue()));
	}

	@Override
	public boolean hasTrigger(Id id) {
		return Utils.send(schema.tiesDB.hasTrigger(ts.getId().getValue(), id.getValue(), id.getValue()));
	}

	@Override
	public List<Field> getFields() {
		List<Field> list = new LinkedList<Field>(); 
		List<byte[]> tss = (List<byte[]>)Utils.send(schema.tiesDB.getTableFieldsKeys(ts.getId().getValue(), id.getValue()));
		for(Iterator<byte[]> it = tss.iterator(); it.hasNext();) {
			Id id = new IdImpl(it.next());
			list.add(new FieldImpl(this, id));
		}
		return list;
	}

	@Override
	public List<Trigger> getTriggers() {
		List<Trigger> list = new LinkedList<Trigger>(); 
		List<byte[]> tss = (List<byte[]>)Utils.send(schema.tiesDB.getTableFieldsKeys(ts.getId().getValue(), id.getValue()));
		for(Iterator<byte[]> it = tss.iterator(); it.hasNext();) {
			Id id = new IdImpl(it.next());
			list.add(new TriggerImpl(this, id));
		}
		return list;
	}

	@Override
	public Field getField(Id id) {
		return new FieldImpl(this, id);
	}

	@Override
	public Trigger getTrigger(Id id) {
		return new TriggerImpl(this, id);
	}

	@Override
	public String getName() {
		return Utils.send(schema.tiesDB.getTableName(ts.getId().getValue(), id.getValue()));
	}

	@Override
	public Tablespace getTablespace() {
		return ts;
	}

}
