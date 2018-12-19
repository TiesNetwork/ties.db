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
package network.tiesdb.transport.impl.ws.netty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import network.tiesdb.transport.api.TiesOutput;

/**
 * TiesDB response handler for WebSock.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class WebSocketOutputHandler implements TiesOutput, AutoCloseable {

    private static class WrappedOutputStream extends ByteArrayOutputStream {

        volatile boolean sentAndClosed = false;
        private final Channel ch;

        private WrappedOutputStream(Channel ch) {
            if (null == ch) {
                throw new NullPointerException("The channel should not be null");
            }
            this.ch = ch;
        }

        private void check() {
            if (sentAndClosed) {
                throw new IllegalStateException("Stream was sent and closed already");
            }
        }

        @Override
        public synchronized void write(int b) {
            check();
            super.write(b);
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) {
            check();
            super.write(b, off, len);
        }

        @Override
        public void close() throws IOException {
            if (!sentAndClosed && size() > 0) {
                try {
                    ch.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(toByteArray()))).sync();
                    sentAndClosed = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            super.close();
        }

    }

    private final WrappedOutputStream os;

    public WebSocketOutputHandler(Channel ch) {
        this.os = new WrappedOutputStream(ch);
    }

    @Override
    public OutputStream getOutputStream() {
        return os;
    }

    @Override
    public void close() throws Exception {
        os.close();
    }

}
