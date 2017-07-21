/*
 * Copyright 2017 Ties BV
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package network.tiesdb.transport.impl.ws.netty;

import java.io.InputStream;

import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import network.tiesdb.transport.api.TiesRequest;

/**
 * TiesDB request handler for WebSock.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class WebSocketRequestHandler implements TiesRequest, AutoCloseable {

	private final InputStream is;

	public WebSocketRequestHandler(WebSocketFrame frame) {
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
