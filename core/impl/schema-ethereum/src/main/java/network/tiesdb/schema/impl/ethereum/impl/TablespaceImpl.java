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

import com.tiesdb.schema.api.Tablespace;

import network.tiesdb.schema.api.TiesSchema;

public class TablespaceImpl implements TiesSchema.Tablespace {

    private final Tablespace ts;

    TablespaceImpl(Tablespace tablespace) {
        this.ts = tablespace;
    }

    public static TiesSchema.Tablespace newInstance(Tablespace tablespace) {
        return null == tablespace ? null : new TablespaceImpl(tablespace);
    }

    @Override
    public String getName() {
        return ts.getName();
    }

    @Override
    public Set<String> getTableNames() {
        return ts.getTables().values().stream().map(t -> t.getName()).collect(Collectors.toCollection(() -> new TreeSet<>()));
    }

    @Override
    public TiesSchema.Table getTable(String name) {
        return TableImpl.newInstance(ts.getTable(name));
    }

}
