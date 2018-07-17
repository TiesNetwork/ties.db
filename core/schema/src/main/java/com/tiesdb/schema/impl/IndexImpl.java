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

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.web3j.tuples.generated.Tuple3;

import com.tiesdb.schema.api.Field;
import com.tiesdb.schema.api.Index;
import com.tiesdb.schema.api.Table;
import com.tiesdb.schema.api.type.Id;

public class IndexImpl extends NamedItemImpl implements Index {
	Table table;
	byte type;
	LinkedHashMap<Id, Field> fields;

	public IndexImpl(TableImpl table, Id id) {
		super(table.schema, id);
		this.table = table; 
	}

	@Override
	protected void load() {
		if(notLoaded()) {
			Tuple3<String, BigInteger, List<byte[]>> t = 
					Utils.send(schema.tiesDB.getIndex(table.getId().getValue(), id.getValue()));
			name = t.getValue1();
			
			type = t.getValue2().byteValue();
			
			LinkedHashMap<Id, Field> tableFields = table.getFields();
			fields = new LinkedHashMap<Id, Field>();

			for(Iterator<byte[]> it=t.getValue3().iterator(); it.hasNext();) {
				Id id = new IdImpl(it.next());
				Field fld = tableFields.get(id);
				if(fld == null)
					throw new RuntimeException("Index field is not contained in the table! Index name: " + name + "; table name: " + table.getTablespace().getName() + "." + table.getName());
				fields.put(id, tableFields.get(id));
			}
		}
	}

	@Override
	public byte getType() {
		load();
		return type;
	}

	@Override
	public LinkedHashMap<Id, Field> getFields() {
		load();
		return fields;
	}

	@Override
	public Table getTable() {
		return table;
	}

}
