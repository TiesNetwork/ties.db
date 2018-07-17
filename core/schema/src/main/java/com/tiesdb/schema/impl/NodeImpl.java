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

import org.web3j.tuples.generated.Tuple2;

import com.tiesdb.schema.api.Node;
import com.tiesdb.schema.api.Ranges;
import com.tiesdb.schema.api.Table;
import com.tiesdb.schema.api.type.Address;
import com.tiesdb.schema.api.type.Id;

public class NodeImpl extends ItemImpl implements Node {
	Address id;
	
	LinkedHashMap<Id, Table> tables;
	LinkedHashMap<Id, Ranges> ranges;
	boolean bInQueue;

	public NodeImpl(SchemaImpl schema, Address id) {
		super(schema);
		this.id = id; 
		this.ranges = new LinkedHashMap<>();
	}

	public NodeImpl(TableImpl table, Address id) {
		super(table.schema);
	}

	@Override
	public LinkedHashMap<Id, Table> getTables() {
		load();
		return tables;
	}

	@Override
	protected void load() {
		if(notLoaded()) {
			Tuple2<Boolean, List<byte[]>> t = Utils.send(schema.tiesDB.getNode(id.toString()));
			
			bInQueue = t.getValue1();
			
			tables = new LinkedHashMap<Id, Table>();
			for(Iterator<byte[]> it=t.getValue2().iterator(); it.hasNext();) {
				Id id = new IdImpl(it.next());
				tables.put(id, schema.createTable(null, id));
			}
		}
		
	}

	@Override
	protected boolean notLoaded() {
		return tables == null;
	}

	@Override
	public Ranges getTableRanges(Id id) {
		Ranges r = ranges.get(id);
		if(r != null)
			return r;
		
		r = new RangesImpl(this, id);
		ranges.put(id, r);
		
		return r;
	}

}
