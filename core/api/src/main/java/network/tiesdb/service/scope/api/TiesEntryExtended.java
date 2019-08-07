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
package network.tiesdb.service.scope.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface TiesEntryExtended extends TiesEntry {

    interface TypedFiled extends Field {

        interface Visitor<T> {

            T on(TypedHashField hashField) throws TiesServiceScopeException;

            T on(TypedValueField valueField) throws TiesServiceScopeException;

        }

        <T> T accept(TiesEntryExtended.TypedFiled.Visitor<T> v) throws TiesServiceScopeException;

        String getType();

    }

    interface TypedHashField extends TypedFiled, HashField {

        @Override
        default <T> T accept(TiesEntryExtended.TypedFiled.Visitor<T> v) throws TiesServiceScopeException {
            return v.on(this);
        }

    }

    interface TypedValueField extends TypedFiled, ValueField {

        Object getObject();

        @SuppressWarnings("unchecked")
        default <T> T get() {
            return (T) getObject();
        }

        @Override
        default <T> T accept(TiesEntryExtended.TypedFiled.Visitor<T> v) throws TiesServiceScopeException {
            return v.on(this);
        }

    }

    String getTablespaceName();

    String getTableName();

    Map<String, TypedHashField> getFieldHashes();

    Map<String, TypedValueField> getFieldValues();

    @Override
    default List<? extends TypedFiled> getFields() {
        return Stream.concat(getFieldHashes().values().stream(), getFieldValues().values().stream()).collect(Collectors.toList());
    }

}