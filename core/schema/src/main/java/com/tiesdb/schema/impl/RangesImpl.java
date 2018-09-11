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
import java.util.LinkedList;
import java.util.List;

import com.tiesdb.schema.api.Range;
import com.tiesdb.schema.api.Ranges;
import com.tiesdb.schema.api.type.Id;

public class RangesImpl extends ItemImpl implements Ranges {
	NodeImpl node;
	Id tableId;
	List<Range> ranges;
	
	public RangesImpl(NodeImpl node, Id tableId) {
		super(node.schema);
		this.node = node;
		this.tableId = tableId;
	}

	@Override
	public List<Range> getRanges() {
		load();
		return ranges;
	}

	@Override
	protected void load() {
		if(notLoaded()) {
			@SuppressWarnings("unchecked")
            List<BigInteger> t = Utils.send(schema.tiesDB.getNodeTableRanges(node.id.toString(), tableId.getValue()));
			ranges = new LinkedList<Range>(); 
			for(Iterator<BigInteger> it = t.iterator(); it.hasNext();) {
				BigInteger val = it.next();
				BigInteger[] vals = val.divideAndRemainder(BigInteger.valueOf(0x100000000L));
				ranges.add(new RangeImpl(vals[0].intValueExact(), vals[1].intValueExact()));
			}
		}
	}

	@Override
	protected boolean notLoaded() {
		return ranges == null;
	}

}
