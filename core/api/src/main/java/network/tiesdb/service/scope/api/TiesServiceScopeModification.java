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

import java.util.Map;

public interface TiesServiceScopeModification extends TiesServiceScopeAction, TiesServiceScopeAction.Distributed {

    interface Entry {

        interface FieldHash {

            byte[] getHash();

            String getType();

        }

        interface FieldValue extends FieldHash {

            Object get();

            byte[] getBytes();

        }

        String getTablespaceName();

        String getTableName();

        TiesEntryHeader getHeader();

        Map<String, FieldHash> getFieldHashes();

        Map<String, FieldValue> getFieldValues();

    }

    interface Result extends TiesServiceScopeResult.Result {

        interface Visitor<T> {

            T on(Success success) throws TiesServiceScopeException;

            T on(Error error) throws TiesServiceScopeException;

        }

        interface Success extends Result {

            @Override
            default <T> T accept(Visitor<T> v) throws TiesServiceScopeException {
                return v.on(this);
            }

        }

        interface Error extends Result {

            @Override
            default <T> T accept(Visitor<T> v) throws TiesServiceScopeException {
                return v.on(this);
            }

            Throwable getError();

        }

        default <T> T accept(TiesServiceScopeResult.Result.Visitor<T> v) throws TiesServiceScopeException {
            return v.on(this);
        }

        <T> T accept(Visitor<T> v) throws TiesServiceScopeException;

        byte[] getHeaderHash();

    }

    Entry getEntry();

    void setResult(Result result) throws TiesServiceScopeException;

}