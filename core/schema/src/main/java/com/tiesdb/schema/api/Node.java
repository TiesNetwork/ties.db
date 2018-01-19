package com.tiesdb.schema.api;

import java.util.List;

import com.tiesdb.schema.api.type.Id;

public interface Node {
	List<Id> getTables();
	Ranges getTableRanges(Id id);
}
