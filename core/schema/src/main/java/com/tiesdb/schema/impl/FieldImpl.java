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
		return Utils.send(schema.tiesDB.getFieldName(table.getId().getValue(), id.getValue()));
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
