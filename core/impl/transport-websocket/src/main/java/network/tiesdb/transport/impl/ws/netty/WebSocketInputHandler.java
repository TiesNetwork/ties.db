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

import java.io.InputStream;

import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import network.tiesdb.transport.api.TiesInput;

/**
 * TiesDB request handler for WebSock.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class WebSocketInputHandler implements TiesInput, AutoCloseable {

    private final InputStream is;

    public WebSocketInputHandler(WebSocketFrame frame) {
        if (null == frame) {
            throw new NullPointerException("The frame should not be null");
        }
        this.is = new ByteBufInputStream(frame.content());
    }

    @Override
    public InputStream getInputStream() {
        return is;
    }

    @Override
    public void close() throws Exception {
        is.close();
    }

}
