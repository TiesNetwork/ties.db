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

public interface TiesServiceScopeRecollection extends TiesServiceScopeAction, TiesServiceScopeAction.Distributed {

    interface Query {

        String getTablespaceName();

        String getTableName();

        List<Selector> getSelectors();

        List<Filter> getFilters();

        interface Value {

            String getType();

            byte[] getRawValue();

            Object getValue() throws TiesServiceScopeException;

        }

        interface Field {

            String getFieldName();

        }

        interface Function {

            interface Argument {

                interface FunctionArgument extends Function, Argument {

                    @Override
                    default <T> T accept(Visitor<T> v) throws TiesServiceScopeException {
                        return v.on(this);
                    }

                }

                interface FieldArgument extends Field, Argument {

                    @Override
                    default <T> T accept(Visitor<T> v) throws TiesServiceScopeException {
                        return v.on(this);
                    }

                }

                interface ValueArgument extends Value, Argument {

                    @Override
                    default <T> T accept(Visitor<T> v) throws TiesServiceScopeException {
                        return v.on(this);
                    }

                }

                interface Visitor<T> {

                    T on(FunctionArgument a) throws TiesServiceScopeException;

                    T on(ValueArgument a) throws TiesServiceScopeException;

                    T on(FieldArgument a) throws TiesServiceScopeException;

                }

                <T> T accept(Visitor<T> v) throws TiesServiceScopeException;

            }

            String getName();

            List<Argument> getArguments();

        }

        public interface Selector {

            interface FunctionSelector extends Function, Selector {

                String getAlias();

                String getType();

                @Override
                default <T> T accept(Visitor<T> v) throws TiesServiceScopeException {
                    return v.on(this);
                }

            }

            interface FieldSelector extends Field, Selector {

                @Override
                default <T> T accept(Visitor<T> v) throws TiesServiceScopeException {
                    return v.on(this);
                }

            }

            interface Visitor<T> {

                T on(FunctionSelector s) throws TiesServiceScopeException;

                T on(FieldSelector s) throws TiesServiceScopeException;

            }

            <T> T accept(Visitor<T> v) throws TiesServiceScopeException;
        }

        interface Filter extends Function {

            String getFieldName();

        }
    }

    Query getQuery() throws TiesServiceScopeException;

    public interface Result extends TiesServiceScopeResult.Result {

        interface Visitor<T> {

            T on(Success success) throws TiesServiceScopeException;

            T on(Error error) throws TiesServiceScopeException;

            T on(Partial partial) throws TiesServiceScopeException;

        }

        interface Field extends TiesEntry.Field {

            interface HashField extends Success.Field, TiesEntry.HashField {

                @Override
                default <T> T accept(Success.Field.Visitor<T> v) throws TiesServiceScopeException {
                    return v.on(this);
                }

                byte[] getHash();

            }

            interface ValueField extends Success.Field {

                @Override
                default <T> T accept(Success.Field.Visitor<T> v) throws TiesServiceScopeException {
                    return v.on(this);
                }

                @Override
                default <T> T accept(TiesEntry.Field.Visitor<T> v) throws TiesServiceScopeException {
                    throw new TiesServiceScopeException(
                            "Internal field representation could not be used as TiesEntry.Field, please use RawField instead");
                }

                Object getFieldValue();

            }

            interface RawField extends Success.Field, TiesEntry.ValueField {

                @Override
                default <T> T accept(Success.Field.Visitor<T> v) throws TiesServiceScopeException {
                    return v.on(this);
                }

                byte[] getRawValue();

                @Override
                default byte[] getValue() {
                    return getRawValue();
                }

            }

            interface Visitor<T> {

                T on(HashField field) throws TiesServiceScopeException;

                T on(RawField field) throws TiesServiceScopeException;

                T on(ValueField field) throws TiesServiceScopeException;

            }

            <T> T accept(Success.Field.Visitor<T> v) throws TiesServiceScopeException;

            String getName();

            String getType();

        }

        interface Entry extends TiesEntry {

            TiesEntryHeader getEntryHeader();

            List<Success.Field> getEntryFields();

            List<Success.Field> getComputedFields();

            @Override
            default TiesEntryHeader getHeader() {
                return getEntryHeader();
            }

            @Override
            default List<? extends Field> getFields() {
                return getEntryFields();
            }

        }

        <T> T accept(Visitor<T> v) throws TiesServiceScopeException;

        @Override
        default <T> T accept(TiesServiceScopeResult.Result.Visitor<T> v) throws TiesServiceScopeException {
            return v.on(this);
        }

    }

    public interface Success extends Result {

        List<Entry> getEntries();

        @Override
        default <T> T accept(Visitor<T> v) throws TiesServiceScopeException {
            return v.on(this);
        }

    }

    public interface Error extends Result {

        List<Throwable> getErrors();

        @Override
        default <T> T accept(Visitor<T> v) throws TiesServiceScopeException {
            return v.on(this);
        }
    }

    public interface Partial extends Success, Error {

        default boolean isSuccess() {
            List<Entry> entries = getEntries();
            return null != entries && !entries.isEmpty();
        }

        default boolean isError() {
            List<Throwable> errors = getErrors();
            return null != errors && !errors.isEmpty();
        }

        @Override
        default <T> T accept(Visitor<T> v) throws TiesServiceScopeException {
            return v.on(this);
        }

    }

    void setResult(Result result) throws TiesServiceScopeException;
}