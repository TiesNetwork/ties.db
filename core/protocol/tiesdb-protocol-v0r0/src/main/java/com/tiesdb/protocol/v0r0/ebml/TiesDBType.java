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
package com.tiesdb.protocol.v0r0.ebml;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import one.utopic.sparse.ebml.EBMLCode;
import one.utopic.sparse.ebml.EBMLType;

public enum TiesDBType implements TiesEBMLType {

    MESSAGE_ID(0xEC, Context.VALUE, Context.REQUEST, Context.RESPONSE, Context.ERROR), // Unsigned

    CONSISTENCY(0xEE, Context.VALUE, Context.REQUEST), // Unsigned

    SIGNATURE(0xFE, Context.VALUE, Context.SIGNED), // Binary
    SIGNER(0xFC, Context.VALUE, Context.SIGNED), // Binary

    ERROR(0x7FFF, Context.ERROR, Context.ROOT), // Meta
    ERROR_MESSAGE(0xE0, Context.VALUE, Context.ERROR), // UTF-8

    MODIFICATION_REQUEST(0x1E544945, Context.MODIFICATION_REQUEST, Context.ROOT), // Meta

    MODIFICATION_ENTRY(0xE1, Context.ENTRY, Context.MODIFICATION_REQUEST), // Meta

    ENTRY_HEADER(0xE1, Context.ENTRY_HEADER, Context.ENTRY), // Meta
    ENTRY_TABLESPACE_NAME(0x80, Context.VALUE, Context.ENTRY_HEADER), // UTF-8
    ENTRY_TABLE_NAME(0x82, Context.VALUE, Context.ENTRY_HEADER), // UTF-8
    //ENTRY_TYPE(0x84, Context.VALUE, Context.ENTRY_HEADER), // Unsigned
    ENTRY_TIMESTAMP(0x86, Context.VALUE, Context.ENTRY_HEADER), // TimeStamp
    ENTRY_VERSION(0x88, Context.VALUE, Context.ENTRY_HEADER), // Unsigned
    ENTRY_OLD_HASH(0x8A, Context.VALUE, Context.ENTRY_HEADER), // Binary (Keccak-256)
    ENTRY_FLD_HASH(0x8C, Context.VALUE, Context.ENTRY_HEADER), // Binary (Tiger-192)
    ENTRY_NETWORK(0x8E, Context.VALUE, Context.ENTRY_HEADER), // Unsigned (BIP-0044/SLIP-0044)

    FIELD_LIST(0xD1, Context.FIELD_LIST, Context.ENTRY), // Meta
    FIELD(0xD1, Context.FIELD, Context.FIELD_LIST), // Meta
    FIELD_NAME(0x80, Context.VALUE, Context.FIELD), // UTF-8
    FIELD_TYPE(0x82, Context.VALUE, Context.FIELD), // ASCII
    FIELD_HASH(0x84, Context.VALUE, Context.FIELD), // Binary (Keccak-256)
    FIELD_VALUE(0x86, Context.VALUE, Context.FIELD), // Binary

    CHEQUE_LIST(0xC1, Context.CHEQUE_LIST, Context.ENTRY), // Meta
    CHEQUE(0xC1, Context.CHEQUE, Context.CHEQUE_LIST), // Meta
    CHEQUE_RANGE(0x80, Context.VALUE, Context.CHEQUE), // Binary (UUID)
    CHEQUE_NUMBER(0x82, Context.VALUE, Context.CHEQUE), // Unsigned
    CHEQUE_TIMESTAMP(0x84, Context.VALUE, Context.CHEQUE), // TimeStamp
    CHEQUE_AMOUNT(0x86, Context.VALUE, Context.CHEQUE), // Unsigned

    ADDRESS_LIST(0xA1, Context.ADDRESS_LIST, Context.CHEQUE), // Meta
    ADDRESS(0xA0, Context.VALUE, Context.ADDRESS_LIST), // Binary

    MODIFICATION_RESPONSE(0x1F544945, Context.MODIFICATION_RESPONSE, Context.ROOT), // Meta

    MODIFICATION_RESULT(0xE1, Context.MODIFICATION_RESULT, Context.MODIFICATION_RESPONSE), // Meta
    MODIFICATION_ERROR(0xEF, Context.MODIFICATION_ERROR, Context.MODIFICATION_RESPONSE), // Meta
    ENTRY_HASH(0x80, Context.VALUE, Context.MODIFICATION_RESULT, Context.MODIFICATION_ERROR), // Binary (Keccak-256)

    RECOLLECTION_REQUEST(0x11544945, Context.RECOLLECTION_REQUEST, Context.ROOT), // Meta

    RECOLLECTION_TABLESPACE_NAME(0x80, Context.VALUE, Context.RECOLLECTION_REQUEST), // UTF-8
    RECOLLECTION_TABLE_NAME(0x82, Context.VALUE, Context.RECOLLECTION_REQUEST), // UTF-8

    RETRIEVE_LIST(0x83, Context.RETRIEVE_LIST, Context.RECOLLECTION_REQUEST), // Meta

    RET_FIELD(0xD0, Context.VALUE, Context.RETRIEVE_LIST), // UTF-8

    RET_COMPUTE(0xC1, Context.RET_COMPUTE, Context.RETRIEVE_LIST), // Meta
    RET_COMPUTE_ALIAS(0xA0, Context.VALUE, Context.RET_COMPUTE), // UTF-8
    RET_COMPUTE_TYPE(0xA2, Context.VALUE, Context.RET_COMPUTE), // ASCII

    FUNCTION_NAME(0xF0, Context.VALUE, Context.FUNCTION), // UTF-8
    FUN_ARGUMENT_FUNCTION(0xF3, Context.FUNCTION, Context.FUNCTION), // Meta
    FUN_ARGUMENT_REFERENCE(0xF2, Context.VALUE, Context.FUNCTION), // UTF-8
    FUN_ARGUMENT_STATIC(0xF1, Context.ARGUMENT_STATIC, Context.FUNCTION), // Meta

    ARG_STATIC_TYPE(0x80, Context.VALUE, Context.ARGUMENT_STATIC), // ASCII
    ARG_STATIC_VALUE(0x82, Context.VALUE, Context.ARGUMENT_STATIC), // Binary

    FILTER_LIST(0xA3, Context.FILTER_LIST, Context.RECOLLECTION_REQUEST), // Meta
    FILTER(0xF1, Context.FILTER, Context.FILTER_LIST), // Meta
    FILTER_FIELD(0xE0, Context.VALUE, Context.FILTER), // UTF-8

    RECOLLECTION_RESPONSE(0x12544945, Context.RECOLLECTION_RESPONSE, Context.ROOT), // Meta
    RECOLLECTION_RESULT(0xA1, Context.RECOLLECTION_RESULT, Context.RECOLLECTION_RESPONSE), // Meta
    RECOLLECTION_ENTRY(0xE1, Context.ENTRY, Context.RECOLLECTION_RESULT), // Meta
    RECOLLECTION_COMPUTE(0xC1, Context.RECOLLECTION_COMPUTE, Context.RECOLLECTION_RESULT), // Meta
    COMPUTE_FIELD(0xC1, Context.FIELD, Context.RECOLLECTION_COMPUTE) // Meta

    ;

    public static enum Context implements EBMLType.Context {

        VALUE {
            @Override
            protected void register(TiesDBType type) {
                throw new UnsupportedOperationException("Type " + type + " should not be registered in context " + this);
            }
        }, //

        ROOT, //

        ERROR, //

        SIGNED, //

        REQUEST, //
        RESPONSE, //

        MODIFICATION_REQUEST(REQUEST), //
        MODIFICATION_RESPONSE(RESPONSE), //

        ENTRY, //
        ENTRY_HEADER(SIGNED), //
        FIELD_LIST, //
        FIELD, //
        ADDRESS_LIST, //
        CHEQUE_LIST, //
        CHEQUE(SIGNED), //

        MODIFICATION_RESULT(SIGNED), //
        MODIFICATION_ERROR(ERROR), //

        RECOLLECTION_REQUEST(REQUEST), //
        RECOLLECTION_RESPONSE(RESPONSE), //

        FUNCTION, //
        ARGUMENT_STATIC, //

        RETRIEVE_LIST, //
        RET_COMPUTE(FUNCTION), //

        FILTER_LIST, //
        FILTER(FUNCTION),

        RECOLLECTION_RESULT(SIGNED), //
        RECOLLECTION_ENTRY, //
        RECOLLECTION_COMPUTE, //

        ;

        static {
            if (null != TiesDBType.class) { // Initialize TiesDB types for all contexts
                // OK
            }
        }

        private final Map<EBMLCode, EBMLType> typeMap = new HashMap<>();
        private final Context[] parentContexts;

        private Context(Context... parentContexts) {
            this.parentContexts = parentContexts;
        }

        @Override
        public EBMLType getType(EBMLCode code) {
            return typeMap.computeIfAbsent(code, c -> {
                EBMLType result = null;
                for (int i = 0; result == null && i < parentContexts.length; i++) {
                    result = parentContexts[i].getType(code);
                }
                return result;
            });
        }

        protected void register(TiesDBType type) {
            if (null != getType(type.code) || typeMap.putIfAbsent(type.code, type) != null) {
                throw new IllegalArgumentException("EventType is already registered for " + type.code);
            }
        }

        @Override
        public boolean contains(EBMLType type) {
            return typeMap.containsValue(type) || parentContains(type);
        }

        private boolean parentContains(EBMLType type) {
            boolean contains = false;
            for (int i = 0; !contains && i < parentContexts.length; i++) {
                contains |= parentContexts[i].contains(type);
            }
            return contains;
        }

    }

    private final Context context;
    private final EBMLCode code;

    private TiesDBType(long code, Context context, Context... regContexts) {
        try {
            this.context = Objects.requireNonNull(context);
            this.code = new EBMLCode(code > 0xFF ? longToBytes(code) : new byte[] { (byte) (0xFF & code) });
            if (Context.VALUE.equals(context) && (code & 1) > 0) {
                throw new IllegalArgumentException("Wrong " + this.code + " for " + this + ". Code should be even for data frames.");
            } else if (!Context.VALUE.equals(context) && (code & 1) == 0) {
                throw new IllegalArgumentException("Wrong " + this.code + " for " + this + ". Code should be odd for structural frames.");
            }
            for (int i = 0; i < regContexts.length; i++) {
                regContexts[i].register(this);
            }
        } catch (Throwable e) {
            throw new TypeInitializationException(this, e);
        }
    }

    @Override
    public EBMLCode getEBMLCode() {
        return code;
    }

    @Override
    public EBMLType.Context getContext() {
        return context;
    }

    private static byte[] longToBytes(long value) {
        if (value == 0) {
            return new byte[0];
        }
        return BigInteger.valueOf(value).toByteArray();
    }

    @Override
    public boolean isStructural() {
        return !Context.VALUE.equals(context);
    }

}

class TypeInitializationException extends LinkageError {

    private static final long serialVersionUID = -77030685922999867L;

    public TypeInitializationException(TiesDBType t, Throwable cause) {
        super("Can't inialize " + t.name() + ": " + cause.getMessage(), cause);
    }

}