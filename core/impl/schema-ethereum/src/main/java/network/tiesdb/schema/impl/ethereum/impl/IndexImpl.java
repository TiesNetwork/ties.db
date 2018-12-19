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

import java.util.List;
import java.util.stream.Collectors;

import com.tiesdb.schema.api.Index;

import network.tiesdb.schema.api.TiesSchema;
import network.tiesdb.schema.api.TiesSchema.IndexType;

public class IndexImpl implements TiesSchema.Index {

    private final Index i;

    IndexImpl(Index i) {
        this.i = i;
    }

    public static TiesSchema.Index newInstance(Index index) {
        return null == index ? null : new IndexImpl(index);
    }

    @Override
    public List<TiesSchema.Field> getFields() {
        return i.getFields().values().stream().map(FieldImpl::newInstance).collect(Collectors.toList());
    }

    @Override
    public IndexType getType() {
        switch (i.getType()) {
        case 1:
            return IndexType.PRIMARY;
        case 2:
            return IndexType.INTERNAL;
        case 4:
            return IndexType.EXTERNAL;
        default:
            throw new IllegalArgumentException("Unknown TiesDB contract IndexType value " + i.getType());
        }
    }

}
