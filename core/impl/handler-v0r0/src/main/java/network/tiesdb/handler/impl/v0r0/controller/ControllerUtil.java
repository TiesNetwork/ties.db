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

import java.math.BigDecimal;
import java.util.function.Function;

import com.tiesdb.protocol.v0r0.ebml.format.UUIDFormat;

import network.tiesdb.service.scope.api.TiesServiceScopeException;
import network.tiesdb.type.Duration;
import one.utopic.sparse.ebml.EBMLFormat;
import one.utopic.sparse.ebml.format.ASCIIStringFormat;
import one.utopic.sparse.ebml.format.BigDecimalFormat;
import one.utopic.sparse.ebml.format.BigIntegerFormat;
import one.utopic.sparse.ebml.format.BytesFormat;
import one.utopic.sparse.ebml.format.DateFormat;
import one.utopic.sparse.ebml.format.DoubleFormat;
import one.utopic.sparse.ebml.format.FloatFormat;
import one.utopic.sparse.ebml.format.IntegerFormat;
import one.utopic.sparse.ebml.format.LongFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

public final class ControllerUtil {

    public static abstract class WriteConverter<T> {

        private final EBMLFormat<T> format;

        public WriteConverter(EBMLFormat<T> format) {
            this.format = format;
        }

        public EBMLFormat<T> getFormat() {
            return format;
        }

        public abstract T convert(Object obj);
    }

    private static final WriteConverter<Integer> BOOLEAN_WP = new WriteConverter<Integer>(IntegerFormat.INSTANCE) {

        @Override
        public Integer convert(Object data) {
            if (data instanceof Boolean) {
                return ((boolean) data) ? 1 : 0;
            } else {
                throw new IllegalArgumentException("Expected Boolean object but was " + data);
            }
        }

    };

    private static final WriteConverter<BigDecimal> DURATION_WP = new WriteConverter<BigDecimal>(BigDecimalFormat.INSTANCE) {

        @Override
        public BigDecimal convert(Object data) {
            if (data instanceof Duration) {
                return ((Duration) data).getInDecimal();
            } else {
                throw new IllegalArgumentException("Expected Duration object but was " + data);
            }
        }

    };

    private ControllerUtil() {
    }

    public static Function<byte[], ?> readerForType(String type) throws TiesServiceScopeException {
        switch (type) {
        case "boolean":
            return data -> IntegerFormat.INSTANCE.readFormat(data) != 0;
        case "duration":
            return data -> new Duration(BigDecimalFormat.INSTANCE.readFormat(data));
        default:
            return getFormatForType(type)::readFormat;
        }
    }

    public static WriteConverter<?> writeConverterForType(String type) throws TiesServiceScopeException {
        switch (type) {
        case "boolean":
            return BOOLEAN_WP;
        case "duration":
            return DURATION_WP;
        default:
            @SuppressWarnings("unchecked")
            WriteConverter<Object> wc = new WriteConverter<Object>((EBMLFormat<Object>) getFormatForType(type)) {
                @Override
                public Object convert(Object obj) {
                    return obj;
                }
            };
            return wc;
        }
    }

    private static EBMLFormat<?> getFormatForType(String type) throws TiesServiceScopeException {
        switch (type) {
        case "int":
        case "integer":
            return IntegerFormat.INSTANCE;
        case "long":
            return LongFormat.INSTANCE;
        case "float":
            return FloatFormat.INSTANCE;
        case "double":
            return DoubleFormat.INSTANCE;
        case "decimal":
            return BigDecimalFormat.INSTANCE;
        case "bigint":
            return BigIntegerFormat.INSTANCE;
        case "string":
            return UTF8StringFormat.INSTANCE;
        case "ascii":
            return ASCIIStringFormat.INSTANCE;
        case "binary":
            return BytesFormat.INSTANCE;
        case "time":
            return DateFormat.INSTANCE;
        case "uuid":
            return UUIDFormat.INSTANCE;
        default:
            throw new TiesServiceScopeException("Unknown type " + type);
        }
    }

}
