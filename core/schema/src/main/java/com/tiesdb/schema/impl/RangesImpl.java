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
