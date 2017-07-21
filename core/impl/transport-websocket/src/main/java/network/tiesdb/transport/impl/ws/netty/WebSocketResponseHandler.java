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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import network.tiesdb.transport.api.TiesResponse;

/**
 * TiesDB response handler for WebSock.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class WebSocketResponseHandler implements TiesResponse, AutoCloseable {

	private static class WrappedOutputStream extends ByteArrayOutputStream {

		volatile boolean sentAndClosed = false;
		private final ChannelHandlerContext ctx;

		private WrappedOutputStream(ChannelHandlerContext ctx) {
			if (null == ctx) {
				throw new NullPointerException("The ctx should not be null");
			}
			this.ctx = ctx;
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
			if (!sentAndClosed) {
				try {
					ctx.channel().writeAndFlush(new TextWebSocketFrame(toString())).sync();
					sentAndClosed = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			super.close();
		}

	}

	private final WrappedOutputStream os;

	public WebSocketResponseHandler(ChannelHandlerContext ctx) {
		this.os = new WrappedOutputStream(ctx);
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
