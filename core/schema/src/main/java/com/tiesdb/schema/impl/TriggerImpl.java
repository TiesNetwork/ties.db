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

import org.web3j.tuples.generated.Tuple2;

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
