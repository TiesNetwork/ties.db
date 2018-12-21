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
package network.tiesdb.schema.impl.ethereum.impl;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.tiesdb.schema.api.Table;

import network.tiesdb.schema.api.TiesSchema;
import network.tiesdb.schema.api.TiesSchema.Field;
import network.tiesdb.schema.api.TiesSchema.Index;

public class TableImpl implements TiesSchema.Table {

    private final Table t;

    TableImpl(Table table) {
        this.t = table;
    }

    public static TiesSchema.Table newInstance(Table tablespace) {
        return null == tablespace ? null : new TableImpl(tablespace);
    }

    @Override
    public String getName() {
        return t.getName();
    }

    @Override
    public Set<String> getFieldNames() {
        return t.getFields().values().stream().map(f -> f.getName()).collect(Collectors.toCollection(() -> new TreeSet<>()));
    }

    @Override
    public Field getField(String name) {
        return FieldImpl.newInstance(t.getField(name));
    }

    @Override
    public Set<Index> getIndexes() {
        return t.getIndexes().values().stream().map(IndexImpl::newInstance).collect(Collectors.toSet());
    }

    @Override
    public boolean isDistributed() {
        return t.isDistributed();
    }

    @Override
    public Set<String> getNodeAddresses() {
        return t.getNodes().keySet().stream().map(a -> a.toChecksumedString()).collect(Collectors.toSet());
    }

}
