package com.tiesdb.schema.api;

import com.tiesdb.schema.api.type.Id;

public interface NamedItem extends Item {
	String getName();
	Id getId();
}
