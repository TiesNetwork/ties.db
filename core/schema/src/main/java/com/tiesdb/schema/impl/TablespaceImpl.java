/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.schema.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.web3j.tuples.generated.Tuple3;
import com.tiesdb.schema.api.Schema;
import com.tiesdb.schema.api.Table;
import com.tiesdb.schema.api.Tablespace;
import com.tiesdb.schema.api.type.Address;
import com.tiesdb.schema.api.type.Id;

public class TablespaceImpl extends NamedItemImpl implements Tablespace {
	LinkedHashMap<Id, Table> tables;
	Address permissions;
	
	public TablespaceImpl(SchemaImpl schema, Id id) {
		super(schema, id);
	}

	@Override
	public boolean hasTable(Id id) {
		load();
		return tables.containsKey(id);
	}

	@Override
	public Table getTable(Id id) {
		load();
		return tables.get(id);
	}

	@Override
	protected void load() {
		if(notLoaded()) {
			if(notLoaded()) {
				Tuple3<String, String, List<byte[]>> t = 
						Utils.send(schema.tiesDB.getTablespace(id.getValue()));
				
				name = t.getValue1();
				
				permissions = new AddressImpl(t.getValue2());
				
				tables = new LinkedHashMap<Id, Table>();
				for(Iterator<byte[]> it=t.getValue3().iterator(); it.hasNext();) {
					Id id = new IdImpl(it.next());
					tables.put(id, schema.createTable(this, id));
				}
			}
		}
	}

	@Override
	public LinkedHashMap<Id, Table> getTables() {
		load();
		return tables;
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public Table getTable(String name) {
		return getTable(schema.idFromName(getName(), name));
	}

}
