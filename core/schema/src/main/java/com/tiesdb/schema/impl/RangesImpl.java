package com.tiesdb.schema.impl;

import java.util.List;

import com.tiesdb.schema.api.Range;
import com.tiesdb.schema.api.Ranges;

public class RangesImpl implements Ranges {
	NodeImpl node;
	List<Range> ranges;
	
	public RangesImpl(NodeImpl node, List<Range> ranges) {
		this.node = node;
		this.ranges = ranges;
	}

	@Override
	public List<Range> getRanges() {
		return ranges;
	}

}
