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

import java.io.IOException;
import java.util.HashSet;
import java.util.function.Consumer;

import one.utopic.abio.api.output.Output;
import one.utopic.sparse.ebml.EBMLWriter;
import one.utopic.sparse.ebml.EBMLWriter.EBMLWriteFormat.Writable;

public class TiesEBMLWriter extends EBMLWriter {

    private final HashSet<Consumer<Byte>> formatListeners = new HashSet<>();

    public boolean addListener(Consumer<Byte> listener) {
        return formatListeners.add(listener);
    }

    public boolean hasListener(Object listener) {
        return formatListeners.contains(listener);
    }

    public boolean removeListener(Object listener) {
        return formatListeners.remove(listener);
    }

    public TiesEBMLWriter(Output out) {
        super(out);
    }

    @Override
    protected Frame.Format newFormatFrame(EBMLWriteFormat.Writable w) {
        if (formatListeners.isEmpty()) {
            return super.newFormatFrame(w);
        } else {
            return new Format(w, getFormatListenersSnapshot());
        }
    }

    @SuppressWarnings("unchecked")
    private Consumer<Byte>[] getFormatListenersSnapshot() {
        return formatListeners.toArray(new Consumer[formatListeners.size()]);
    }

    protected class Format extends Frame.Format {

        private final Consumer<Byte>[] listeners;

        @SafeVarargs
        public Format(Writable writable, Consumer<Byte>... listeners) {
            super(writable);
            this.listeners = listeners;
        }

        @Override
        public void write(Output out) throws IOException {
            super.write(new Output() {

                @Override
                public void writeByte(byte b) throws IOException {
                    for (Consumer<Byte> listener : listeners) {
                        listener.accept(b);
                    }
                }

                @Override
                public boolean isFinished() {
                    return false;
                }

            });
            super.write(out);
        }

    }

}
