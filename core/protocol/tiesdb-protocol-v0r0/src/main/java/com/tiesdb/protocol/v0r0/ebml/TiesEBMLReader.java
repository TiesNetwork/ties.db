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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import one.utopic.abio.api.input.Input;
import one.utopic.abio.api.output.Output;
import one.utopic.sparse.api.exception.SparseReaderException;
import one.utopic.sparse.ebml.EBMLCode;
import one.utopic.sparse.ebml.EBMLEvent;
import one.utopic.sparse.ebml.EBMLReader;
import one.utopic.sparse.ebml.EBMLType;

public class TiesEBMLReader extends EBMLReader {

    public static final EBMLReadFormat<Void> SKIP = new EBMLReadFormat<Void>() {

        @Override
        public Void readFormat(byte[] data) {
            return null;
        }

    };

    private final WrappedInput win;
    private boolean skipUnknownTag = false;

    public TiesEBMLReader(Input in, EBMLType.Context context) {
        this(new WrappedInput(in), context);
    }

    protected TiesEBMLReader(WrappedInput win, EBMLType.Context context) {
        super(requireNonNull(win), context);
        this.win = win;
    }

    public boolean addListener(Consumer<Byte> listener) {
        return this.win.listeners.add(listener);
    }

    public boolean hasListener(Object listener) {
        return this.win.listeners.contains(listener);
    }

    public boolean removeListener(Object listener) {
        return this.win.listeners.remove(listener);
    }

    public boolean isSkipUnknownTag() {
        return skipUnknownTag;
    }

    public void setSkipUnknownTag(boolean skipUnknownTags) {
        this.skipUnknownTag = skipUnknownTags;
    }

    @Override
    public EBMLEvent next() {
        EBMLEvent event;
        EBMLType type;
        while (null != (event = super.next()) &&  (type = event.get()) instanceof UnknownTiesEBMLType && skipUnknownTag) {
            UnknownTiesEBMLType unknownType = (UnknownTiesEBMLType) type;
            if (hasNext()) {
                if (unknownType.isStructural()) {
                    event = super.next();
                } else {
                    SKIP.read(this);
                }
            }
        }
        return event;
    }

    @Override
    protected EBMLType resolveTypeCode(EBMLType type, EBMLCode code) throws SparseReaderException {
        if (null != type) {
            return super.resolveTypeCode(type, code);
        }

        AtomicReference<Byte> lastByteRef = new AtomicReference<Byte>();
        try {
            code.write(new Output() {

                @Override
                public void writeByte(byte b) throws IOException {
                    lastByteRef.set(b);
                }

                @Override
                public boolean isFinished() {
                    return false;
                }

            });
        } catch (IOException e) {
            throw new SparseReaderException(e);
        }

        return Optional.ofNullable(lastByteRef.get()) //
                .map(b -> {
                    return new UnknownTiesEBMLType(code, b % 2 > 0);
                }) //
                .orElseThrow(() -> new SparseReaderException("Empty EBML code"));
    }

    @Override
    protected <O> O read(EBMLReadFormat<O> ebmlReadFormat) {
        this.win.capture = true;
        O o = super.read(ebmlReadFormat);
        this.win.capture = false;
        return o;
    }

    public static class UnknownTiesEBMLType implements TiesEBMLType {

        private final EBMLCode code;
        private final boolean structural;

        public UnknownTiesEBMLType(EBMLCode code, boolean structural) {
            super();
            this.code = code;
            this.structural = structural;
        }

        @Override
        public EBMLCode getEBMLCode() {
            return code;
        }

        @Override
        public Context getContext() {
            return new Context() {

                @Override
                public EBMLType getType(EBMLCode code) {
                    return null;
                }

                @Override
                public boolean contains(EBMLType type) {
                    return structural;
                }

                @Override
                public boolean is(Context context) {
                    return equals(context);
                }

            };
        }

        @Override
        public boolean isStructural() {
            return structural;
        }

        @Override
        public String toString() {
            return "UnknownTiesEBMLType [code=" + code + ", structural=" + structural + "]";
        }

    }

    protected static class WrappedInput implements Input {

        public boolean capture;
        private final Input in;
        private final Set<Consumer<Byte>> listeners = new HashSet<>();

        public WrappedInput(Input in) {
            this.in = requireNonNull(in);
        }

        public boolean isFinished() {
            return in.isFinished();
        }

        public byte readByte() throws IOException {
            byte b = in.readByte();
            if (capture) {
                for (Consumer<Byte> listener : listeners) {
                    listener.accept(b);
                }
            }
            return b;
        }

    }

}
