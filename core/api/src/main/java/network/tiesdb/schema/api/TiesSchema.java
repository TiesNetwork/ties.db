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
package network.tiesdb.schema.api;

import java.util.List;
import java.util.Set;

public interface TiesSchema {

    public enum IndexType {
        PRIMARY, INTERNAL, EXTERNAL
    }

    interface Field {

        String getName();

        String getType();

    }

    interface Index {

        List<Field> getFields();

        IndexType getType();

    }

    interface Table {

        String getName();

        Set<String> getFieldNames();

        Field getField(String name);

        Set<Index> getIndexes();

        Set<String> getNodeAddresses();

        boolean isDistributed();

    }

    interface Tablespace {

        String getName();

        Set<String> getTableNames();

        Table getTable(String name);

    }

    short getSchemaNetwork();

    Tablespace getTablespace(String name);

}
