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
package com.tiesdb.protocol.v0r0.ebml.format;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import one.utopic.abio.api.output.Output;
import one.utopic.sparse.ebml.EBMLFormat;

public class FixedSupplierFormat<T> implements EBMLFormat<Supplier<T>> {

    private final EBMLFormat<T> format;
    private final int size;

    public FixedSupplierFormat(EBMLFormat<T> format, int size) {
        this.format = format;
        this.size = size;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + format.toString() + "]";
    }

    @Override
    public Writable getWritable(final Supplier<T> supplier) {
        return new Writable() {

            private final AtomicReference<Writable> wRef = new AtomicReference<>();

            private Writable getWritable() {
                Writable w = wRef.get();
                return null != w ? w : wRef.updateAndGet(new UnaryOperator<Writable>() {
                    @Override
                    public Writable apply(Writable w) {
                        if (null == w) {
                            return format.getWritable(supplier.get());
                        } else {
                            return w;
                        }
                    }
                });
            }

            @Override
            public void writeFormat(Output out) throws IOException {
                getWritable().writeFormat(new Output() {

                    private int count = size;

                    @Override
                    public void writeByte(byte b) throws IOException {
                        if (isFinished()) {
                            throw new IOException("EOF");
                        }
                        out.writeByte(b);
                    }

                    @Override
                    public boolean isFinished() {
                        return count <= 0;
                    }
                });
            }

            @Override
            public int getSize() {
                return size;
            }

        };
    }

    @Override
    public Supplier<T> readFormat(byte[] data) {
        return () -> {
            return format.readFormat(data);
        };
    }

}
