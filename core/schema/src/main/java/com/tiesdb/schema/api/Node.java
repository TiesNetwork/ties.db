package com.tiesdb.schema.api;

import java.util.LinkedHashMap;
import java.util.List;

import com.tiesdb.schema.api.type.Id;

public interface Node extends Item {
	LinkedHashMap<Id, Table> getTables();
	Ranges getTableRanges(Id id);
}
