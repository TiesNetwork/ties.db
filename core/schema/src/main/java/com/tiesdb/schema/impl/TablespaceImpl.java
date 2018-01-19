package com.tiesdb.schema.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.tiesdb.schema.api.Table;
import com.tiesdb.schema.api.Tablespace;
import com.tiesdb.schema.api.type.Id;

public class TablespaceImpl extends NamedItemImpl implements Tablespace {
	public TablespaceImpl(SchemaImpl schema, Id id) {
		super(schema, id);
	}

	@Override
	public boolean hasTable(Id id) {
		return Utils.send(schema.tiesDB.hasTable(this.id.getValue(), id.getValue()));
	}

	@Override
	public List<Table> getTables() {
		List<Table> list = new LinkedList<Table>(); 
		List<byte[]> tbls = (List<byte[]>) Utils.send(schema.tiesDB.getTablespaceTablesKeys(id.getValue()));
		for(Iterator it = tbls.iterator(); it.hasNext();) {
			Id id = new IdImpl((byte[])it.next());
			list.add(new TableImpl(this, id));
		}
		return list;
	}

	@Override
	public Table getTable(Id id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return Utils.send(schema.tiesDB.getTablespaceName(id.getValue()));
	}

}
