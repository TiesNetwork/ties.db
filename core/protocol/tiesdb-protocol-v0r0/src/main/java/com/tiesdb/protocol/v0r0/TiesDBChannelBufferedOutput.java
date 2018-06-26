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
package com.tiesdb.protocol.v0r0;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;

import com.tiesdb.protocol.api.TiesDBProtocol.TiesDBChannelOutput;

public class TiesDBChannelBufferedOutput implements TiesDBChannelOutput {

    private final TiesDBChannelOutput upstream;
    private volatile ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public TiesDBChannelBufferedOutput(TiesDBChannelOutput upstream) {
        this.upstream = Objects.requireNonNull(upstream);
    }

    @Override
    public boolean isFinished() {
        return isClosed();
    }

    @Override
    public void writeByte(byte b) throws IOException {
        checkNotClosed();
        baos.write(b);
    }

    @Override
    public void flush() throws IOException {
        checkNotClosed();
        byte[] ba;
        synchronized (baos) {
            ba = this.baos.toByteArray();
            baos.reset();
        }
        synchronized (upstream) {
            for (int i = 0; i < ba.length; i++) {
                upstream.writeByte(ba[i]);
            }
            upstream.flush();
        }
    }

    @Override
    public boolean isClosed() {
        return null == this.baos;
    }

    @Override
    public void close() throws IOException {
        this.baos = null;
    }

    private void checkNotClosed() throws IOException {
        if (this.isClosed()) {
            throw new ClosedChannelException();
        }
    }
}
