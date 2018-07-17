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
package com.tiesdb.schema.api;

import java.util.LinkedHashMap;

import com.tiesdb.schema.api.type.Address;
import com.tiesdb.schema.api.type.Id;

public interface Table extends NamedItem {
	boolean hasField(Id id);
	boolean hasField(String name);
	boolean hasTrigger(Id id);
	boolean hasTrigger(String name);
	boolean hasIndex(Id id);
	boolean hasIndex(String name);
	
	LinkedHashMap<Id, Field> getFields();
	LinkedHashMap<Id, Trigger> getTriggers();
	LinkedHashMap<Id, Index> getIndexes();
	LinkedHashMap<Address, Node> getNodes();
	
	Field getField(Id id);
	Field getField(String name);
	Trigger getTrigger(Id id);
	Trigger getTrigger(String name);
	Index getIndex(Id id);
	Index getIndex(String name);
	Node getNode(Address id);
	
	Tablespace getTablespace();
    boolean isDistributed();
}
