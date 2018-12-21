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
package network.tiesdb.handler.impl.v0r0.util;

import java.io.IOException;
import java.io.InputStream;

import com.tiesdb.protocol.api.TiesDBProtocol.TiesDBChannelInput;

public class StreamInput implements TiesDBChannelInput {

    private final InputStream is;
    private volatile boolean isClosed = false;

    public StreamInput(InputStream is) {
        this.is = is;
    }

    @Override
    public boolean isFinished() {
        if (isClosed) {
            return true;
        }
        try {
            is.mark(1);
            return is.read() == -1;
        } catch (IOException e) {
            return true;
        } finally {
            try {
                is.reset();
            } catch (IOException e) {
                return true;
            }
        }
    }

    @Override
    public byte readByte() throws IOException {
        return (byte) is.read();
    }

    @Override
    public int skip(int byteCount) throws IOException {
        return (int) is.skip(byteCount);
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
    }

}
