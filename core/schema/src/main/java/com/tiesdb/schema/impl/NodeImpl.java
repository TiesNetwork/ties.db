package com.tiesdb.schema.impl;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.tiesdb.schema.api.Node;
import com.tiesdb.schema.api.Range;
import com.tiesdb.schema.api.Ranges;
import com.tiesdb.schema.api.type.Address;
import com.tiesdb.schema.api.type.Id;

public class NodeImpl implements Node {
	SchemaImpl schema;
	Address id;

	public NodeImpl(SchemaImpl schema, Address id) {
		this.schema = schema;
		this.id = id; 
	}

	@Override
	public List<Id> getTables() {
		return null;
	}

	@Override
	public Ranges getTableRanges(Id id) {
		List<Range> list = new LinkedList<Range>(); 
		List<byte[]> tbls = (List<byte[]>) Utils.send(schema.tiesDB.getNodeTableRanges(this.id.toString(), id.getValue()));
		for(Iterator<byte[]> it = tbls.iterator(); it.hasNext();) {
			BigInteger val = new BigInteger(it.next());
			BigInteger[] vals = val.divideAndRemainder(BigInteger.valueOf(0x100000000L));
			list.add(new RangeImpl(vals[0].intValueExact(), vals[1].intValueExact()));
		}
		return new RangesImpl(this, list);
	}

}
