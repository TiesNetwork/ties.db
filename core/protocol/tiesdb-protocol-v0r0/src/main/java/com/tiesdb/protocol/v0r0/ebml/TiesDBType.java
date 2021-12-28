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

    UNKNOWN_STRUCTURE(Context.UNKNOWN_STRUCTURE), // Meta
    UNKNOWN_VALUE(Context.VALUE, Context.UNKNOWN_STRUCTURE), // Binary

    MESSAGE_ID(0xEC, Context.VALUE, Context.IDENTIFIED_MESSAGE), // Unsigned

    CONSISTENCY(0xEE, Context.VALUE, Context.COORDINATED_REQUEST), // Unsigned

    SIGNATURE(0xFE, Context.VALUE, Context.SIGNED), // Binary
    SIGNER(0xFC, Context.VALUE, Context.SIGNED), // Binary (Address)

    ERROR(0x7FFF, Context.ERROR, Context.ROOT), // Meta
    ERROR_MESSAGE(0xE0, Context.VALUE, Context.ERROR), // UTF-8

    TABLESPACE_NAME(0x80, Context.VALUE, Context.TABLE_META), // UTF-8
    TABLE_NAME(0x82, Context.VALUE, Context.TABLE_META), // UTF-8
    FIELD_NAME(0x80, Context.VALUE, Context.FIELD_META), // UTF-8
    FIELD_TYPE(0x82, Context.VALUE, Context.FIELD_META), // ASCII

    ENTRY(0xE1, Context.ENTRY, Context.ENTRY_CONTAINER), // Meta
    ENTRY_HEADER(0xE1, Context.ENTRY_HEADER, Context.ENTRY), // Meta
    ENTRY_TIMESTAMP(0x86, Context.VALUE, Context.ENTRY_HEADER), // TimeStamp
    ENTRY_VERSION(0x88, Context.VALUE, Context.ENTRY_HEADER), // Unsigned
    ENTRY_OLD_HASH(0x8A, Context.VALUE, Context.ENTRY_HEADER), // Binary (Keccak-256)
    ENTRY_FLD_HASH(0x8C, Context.VALUE, Context.ENTRY_HEADER), // Binary (Tiger-192)
    ENTRY_NETWORK(0x8E, Context.VALUE, Context.ENTRY_HEADER), // Unsigned (BIP-0044/SLIP-0044)
    FIELD_LIST(0xD1, Context.FIELD_LIST, Context.ENTRY), // Meta
    FIELD(0xD1, Context.FIELD, Context.FIELD_LIST), // Meta
    FIELD_HASH(0x84, Context.VALUE, Context.FIELD), // Binary (Keccak-256)
    FIELD_VALUE(0x86, Context.VALUE, Context.FIELD), // Binary

    ENTRY_HASH(0x80, Context.VALUE, Context.ENTRY_REFERENCE), // Binary (Keccak-256)

    CHEQUE_LIST(0xC1, Context.CHEQUE_LIST, Context.CHEQUE_LIST_CONTAINER), // Meta
    CHEQUE(0xC1, Context.CHEQUE, Context.CHEQUE_LIST), // Meta
    CHEQUE_VERSION(0xC0, Context.VALUE, Context.CHEQUE), // Unsigned
    CHEQUE_NETWORK(0xC2, Context.VALUE, Context.CHEQUE), // Unsigned (BIP-0044/SLIP-0044)
    CHEQUE_SESSION(0xC4, Context.VALUE, Context.CHEQUE), // Session (UUID)
    CHEQUE_NUMBER(0xC6, Context.VALUE, Context.CHEQUE), // Unsigned (NOnce per Session)
    CHEQUE_CRP_AMOUNT(0xC8, Context.VALUE, Context.CHEQUE), // Unsigned

    MODIFICATION_REQUEST(0x1E544945, Context.MODIFICATION_REQUEST, Context.ROOT), // Meta

    MODIFICATION_RESPONSE(0x1F544945, Context.MODIFICATION_RESPONSE, Context.ROOT), // Meta
    MODIFICATION_RESULT(0xE1, Context.MODIFICATION_RESULT, Context.MODIFICATION_RESPONSE), // Meta
    MODIFICATION_ERROR(0xEF, Context.MODIFICATION_ERROR, Context.MODIFICATION_RESPONSE), // Meta

    RECOLLECTION_REQUEST(0x11544945, Context.RECOLLECTION_REQUEST, Context.ROOT), // Meta

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
    RECOLLECTION_ERROR(0xAF, Context.RECOLLECTION_ERROR, Context.RECOLLECTION_RESPONSE), // Meta
    RECOLLECTION_COMPUTE(0xC1, Context.RECOLLECTION_COMPUTE, Context.RECOLLECTION_RESULT), // Meta
    COMPUTE_FIELD(0xC1, Context.FIELD, Context.RECOLLECTION_COMPUTE), // Meta

    SCHEMA_REQUEST(0x16544945, Context.SCHEMA_REQUEST, Context.ROOT), // Meta

    SCHEMA_RESPONSE(0x15544945, Context.SCHEMA_RESPONSE, Context.ROOT), // Meta
    SCHEMA_FIELD(0xD1, Context.SCHEMA_FIELD, Context.SCHEMA_RESPONSE), // Meta
    SCHEMA_KEY_FIELD(0xD3, Context.SCHEMA_FIELD, Context.SCHEMA_RESPONSE), // Binary

    HEALING_REQUEST(0x1C544945, Context.HEALING_REQUEST, Context.ROOT), // Meta
    HEALING_RESPONSE(0x1D544945, Context.HEALING_RESPONSE, Context.ROOT), // Meta
    HEALING_RESULT(0xE1, Context.HEALING_RESULT, Context.HEALING_RESPONSE), // Meta
    HEALING_ERROR(0xEF, Context.HEALING_ERROR, Context.HEALING_RESPONSE), // Meta

    ;

    public static enum Context implements EBMLType.Context {

        VALUE {
            @Override
            protected void register(TiesDBType type) {
                throw new UnsupportedOperationException("Type " + type + " should not be registered in context " + this);
            }
        }, //

        UNKNOWN_STRUCTURE,

        ROOT, //

        SIGNED, //

        IDENTIFIED_MESSAGE, //
        CONSISTENT, //

        ERROR(IDENTIFIED_MESSAGE), //

        REQUEST(IDENTIFIED_MESSAGE), //
        COORDINATED_REQUEST(CONSISTENT, REQUEST), //
        RESPONSE(IDENTIFIED_MESSAGE), //

        TABLE_META, //

        CHEQUE_LIST_CONTAINER, //
        CHEQUE_LIST, //
        CHEQUE(SIGNED, TABLE_META), //

        ENTRY_REFERENCE,

        ENTRY_CONTAINER, //
        //SIGNED_ENTRY_CONTAINER(SIGNED, ENTRY_CONTAINER), //
        ENTRY(CHEQUE_LIST_CONTAINER), //
        ENTRY_HEADER(SIGNED, TABLE_META), //
        FIELD_LIST, //
        FIELD_META, //
        FIELD(FIELD_META), //

        MODIFICATION_REQUEST(COORDINATED_REQUEST, ENTRY_CONTAINER), //
        MODIFICATION_RESPONSE(RESPONSE), //
        MODIFICATION_RESULT(SIGNED, ENTRY_REFERENCE), //
        MODIFICATION_ERROR(ERROR, ENTRY_REFERENCE), //

        RECOLLECTION_REQUEST(COORDINATED_REQUEST, TABLE_META), //
        RECOLLECTION_RESPONSE(RESPONSE), //
        RECOLLECTION_ERROR(ERROR, ENTRY_REFERENCE), //

        FUNCTION, //
        ARGUMENT_STATIC, //

        RETRIEVE_LIST, //
        RET_COMPUTE(FUNCTION), //

        FILTER_LIST, //
        FILTER(FUNCTION),

        RECOLLECTION_RESULT(SIGNED, ENTRY_CONTAINER), //
        RECOLLECTION_COMPUTE, //

        SCHEMA_REQUEST(COORDINATED_REQUEST, TABLE_META), //
        SCHEMA_FIELD(FIELD_META), //
        SCHEMA_RESPONSE(RESPONSE, TABLE_META), //

        HEALING_REQUEST(REQUEST, SIGNED, ENTRY_CONTAINER), //
        HEALING_RESPONSE(RESPONSE), //
        HEALING_RESULT(SIGNED, ENTRY_REFERENCE), //
        HEALING_ERROR(ERROR, ENTRY_REFERENCE), //

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

        @Override
        public boolean is(EBMLType.Context context) {
            return equals(context) || parentIs(context);
        }

        private boolean parentIs(EBMLType.Context context) {
            boolean is = false;
            for (int i = 0; !is && i < parentContexts.length; i++) {
                is |= parentContexts[i].is(context);
            }
            return is;
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

    private TiesDBType(Context context, Context... regContexts) {
        this(0, context, regContexts);
    }

    private TiesDBType(long code, Context context, Context... regContexts) {
        try {
            this.context = Objects.requireNonNull(context);
            if (this.name().startsWith("UNKNOWN_")) {
                if (0 != code) {
                    throw new IllegalArgumentException("Wrong code for type " + this + ". Unknown types code should not be set.");
                }
                this.code = null;
            } else {
                EBMLCode ebmlCode = new EBMLCode(code > 0xFF ? longToBytes(code) : new byte[] { (byte) (0xFF & code) });
                if (Context.VALUE.equals(context) && (code & 1) > 0) {
                    throw new IllegalArgumentException("Wrong " + ebmlCode + " for " + this + ". Code should be even for data frames.");
                } else if (!Context.VALUE.equals(context) && (code & 1) == 0) {
                    throw new IllegalArgumentException(
                            "Wrong " + ebmlCode + " for " + this + ". Code should be odd for structural frames.");
                }
                this.code = ebmlCode;
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
