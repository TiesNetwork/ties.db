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
package network.tiesdb.handler.impl.v0r0.controller;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tiesdb.protocol.v0r0.reader.ComputeRetrieveReader.ComputeRetrieve;
import com.tiesdb.protocol.v0r0.reader.FieldRetrieveReader.FieldRetrieve;
import com.tiesdb.protocol.v0r0.reader.FunctionReader.ArgumentFunction;
import com.tiesdb.protocol.v0r0.reader.FunctionReader.ArgumentReference;
import com.tiesdb.protocol.v0r0.reader.FunctionReader.ArgumentStatic;
import com.tiesdb.protocol.v0r0.reader.FunctionReader.FunctionArgument;
import com.tiesdb.protocol.v0r0.reader.RecollectionRequestReader.RecollectionRequest;
import com.tiesdb.protocol.v0r0.reader.RecollectionRequestReader.Retrieve;
import com.tiesdb.protocol.v0r0.reader.FilterReader.Filter;
import com.tiesdb.protocol.v0r0.reader.ChequeReader.Cheque;

import network.tiesdb.service.scope.api.TiesCheque;
import network.tiesdb.service.scope.api.TiesServiceScopeException;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Query;

import static network.tiesdb.handler.impl.v0r0.controller.QueryImplHelper.*;
import static java.util.Collections.*;

public class QueryImpl implements Query {

    private final List<Query.Selector> selectors;
    private final List<Query.Filter> filters;
    private final List<TiesCheque> cheques;
    private final String tablespaceName;
    private final String tableName;

    public QueryImpl(RecollectionRequest request) {
        this.tablespaceName = request.getTablespaceName();
        this.tableName = request.getTableName();
        this.selectors = unmodifiableList(convertSelectors(request.getRetrieves(), Collectors.toList()));
        this.filters = unmodifiableList(convertFilters(request.getFilters(), Collectors.toList()));
        this.cheques = unmodifiableList(convertCheques(request.getCheques(), Collectors.toList()));
    }

    @Override
    public String getTablespaceName() {
        return this.tablespaceName;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }

    @Override
    public List<Selector> getSelectors() {
        return this.selectors;
    }

    @Override
    public List<Filter> getFilters() {
        return this.filters;
    }

    @Override
    public List<? extends TiesCheque> getCheques() {
        return this.cheques;
    }

}

class QueryImplHelper {

    private static <T> Stream<T> asStream(Collection<T> c) {
        return c.parallelStream();
    }

    static <R, A> R convertCheques(List<Cheque> from, Collector<TiesCheque, A, R> c) {
        return asStream(from).map(cheque -> new TiesCheque() {

            @Override
            public BigInteger getChequeVersion() {
                return cheque.getChequeVersion();
            }

            @Override
            public BigInteger getChequeNetwork() {
                return cheque.getChequeNetwork();
            }

            @Override
            public UUID getChequeSession() {
                return cheque.getChequeSession();
            }

            @Override
            public BigInteger getChequeNumber() {
                return cheque.getChequeNumber();
            }

            @Override
            public BigInteger getChequeCropAmount() {
                return cheque.getChequeCropAmount();
            }

            @Override
            public String getTablespaceName() {
                return cheque.getTablespaceName();
            }

            @Override
            public String getTableName() {
                return cheque.getTableName();
            }

            @Override
            public byte[] getSigner() {
                return cheque.getSigner();
            }

            @Override
            public byte[] getSignature() {
                return cheque.getSignature();
            }

        }).collect(c);

    }

    static <R, A> R convertFilters(List<Filter> from, Collector<Query.Filter, A, R> c) {
        return asStream(from).map(f -> new Query.Filter() {

            private final List<Argument> arguments = unmodifiableList(convertArguments(f.getArguments(), Collectors.toList()));

            @Override
            public String getName() {
                return f.getName();
            }

            @Override
            public List<Argument> getArguments() {
                return arguments;
            }

            @Override
            public String getFieldName() {
                return f.getFieldName();
            }

        }).collect(c);
    }

    static <R, A> R convertSelectors(List<Retrieve> from, Collector<Query.Selector, A, R> c) {
        return asStream(from)//
                .map(r -> r.accept(new Retrieve.Visitor<Query.Selector>() {

                    @Override
                    public Query.Selector on(FieldRetrieve retrieve) {
                        return new Query.Selector.FieldSelector() {
                            @Override
                            public String getFieldName() {
                                return retrieve.getFieldName();
                            }
                        };
                    }

                    @Override
                    public Query.Selector on(ComputeRetrieve retrieve) {
                        return new Query.Selector.FunctionSelector() {

                            List<Argument> arguments = unmodifiableList(convertArguments(retrieve.getArguments(), Collectors.toList()));

                            @Override
                            public String getName() {
                                return retrieve.getName();
                            }

                            @Override
                            public List<Argument> getArguments() {
                                return arguments;
                            }

                            @Override
                            public String getAlias() {
                                return retrieve.getAlias();
                            }

                            @Override
                            public String getType() {
                                return retrieve.getType();
                            }

                        };
                    }
                })).collect(c);
    }

    static <R, A> R convertArguments(List<FunctionArgument> from, Collector<Query.Function.Argument, A, R> c) {
        return asStream(from)//
                .map(a -> a.accept(new FunctionArgument.Visitor<Query.Function.Argument>() {

                    @Override
                    public Query.Function.Argument on(ArgumentFunction arg) {
                        return new Query.Function.Argument.FunctionArgument() {

                            List<Argument> arguments = unmodifiableList(
                                    convertArguments(arg.getFunction().getArguments(), Collectors.toList()));

                            @Override
                            public String getName() {
                                return arg.getFunction().getName();
                            }

                            @Override
                            public List<Argument> getArguments() {
                                return arguments;
                            }

                        };
                    }

                    @Override
                    public Query.Function.Argument on(ArgumentReference arg) {
                        return new Query.Function.Argument.FieldArgument() {

                            @Override
                            public String getFieldName() {
                                return arg.getFieldName();
                            }

                        };
                    }

                    @Override
                    public Query.Function.Argument on(ArgumentStatic arg) {
                        return new Query.Function.Argument.ValueArgument() {

                            @Override
                            public Object getValue() throws TiesServiceScopeException {
                                return ControllerUtil.readerForType(getType()).apply(getRawValue());
                            }

                            @Override
                            public String getType() {
                                return arg.getType();
                            }

                            @Override
                            public byte[] getRawValue() {
                                return arg.getRawValue();
                            }

                        };
                    }
                })).collect(c);
    }
}
