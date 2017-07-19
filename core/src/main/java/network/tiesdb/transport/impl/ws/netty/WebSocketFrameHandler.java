/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package network.tiesdb.transport.impl.ws.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import network.tiesdb.transport.api.TiesTransport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Echoes uppercase content of text frames.
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketFrameHandler.class);

	private final TiesTransport transport;
    
    public WebSocketFrameHandler(TiesTransport transport){
		if (null == transport) {
			throw new NullPointerException("The transport should not be null");
		}
		this.transport = transport;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // ping and pong frames already handled

        if (frame instanceof TextWebSocketFrame || frame instanceof BinaryWebSocketFrame) {
            // Send the uppercase string back.
            //String request = ((TextWebSocketFrame) frame).text();
            logger.info("{} received {} bytes", ctx.channel(), frame.content().readableBytes());
            
            transport.handle(new WebSocketRequestHandler(frame), new WebSocketResponseHandler(ctx));
            
            //ctx.channel().writeAndFlush(new TextWebSocketFrame(request.toUpperCase(Locale.US)));
        } else {
            throw new UnsupportedOperationException("unsupported frame type: " + frame.getClass().getName());
        }
    }
}
