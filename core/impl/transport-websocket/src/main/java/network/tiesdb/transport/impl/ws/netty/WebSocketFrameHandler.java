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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import network.tiesdb.exception.TiesException;
import network.tiesdb.service.scope.api.TiesServiceScopeConsumer;
import network.tiesdb.transport.impl.ws.TiesTransportImpl;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketFrameHandler.class);

    protected final TiesTransportImpl transport;

    public WebSocketFrameHandler(TiesTransportImpl transport) {
        if (null == transport) {
            throw new NullPointerException("The transport should not be null");
        }
        this.transport = transport;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame inboundFrame) throws Exception {
        ByteBuf content = inboundFrame.content();
        if (inboundFrame instanceof PingWebSocketFrame) {
            logger.debug("{} ping", ctx.channel());
            if (content.readableBytes() == Long.BYTES) {
                ByteBuffer timestampBuf = ByteBuffer.allocate(Long.BYTES);
                content.getBytes(0, timestampBuf);
                timestampBuf.flip();
                logger.debug("{} pinged timestamp {}", ctx.channel(), timestampBuf.getLong());
                ctx.channel().writeAndFlush(new PongWebSocketFrame(Unpooled.wrappedBuffer(timestampBuf.array())));
            } else {
                logger.warn("{} can't read timestamp from ping", ctx.channel());
                ctx.channel().writeAndFlush(new PongWebSocketFrame());
            }
        } else if (inboundFrame instanceof PongWebSocketFrame) {
            logger.debug("{} pong", ctx.channel());
            if (content.readableBytes() == Long.BYTES) {
                ByteBuffer timestampBuf = ByteBuffer.allocate(Long.BYTES);
                content.getBytes(0, timestampBuf);
                timestampBuf.flip();
                logger.debug("{} ponged timestamp {}", ctx.channel(), timestampBuf.getLong());
            } else {
                logger.warn("{} can't read timestamp from pong", ctx.channel());
            }
        } else if (inboundFrame instanceof BinaryWebSocketFrame) {
            logger.trace("{} received {} bytes", ctx.channel(), content.readableBytes());
            BinaryWebSocketFrame frame = (BinaryWebSocketFrame) inboundFrame;
            try {
                try (WebSocketInputHandler request = new WebSocketInputHandler(frame)) {
                    try (WebSocketOutputHandler response = new WebSocketOutputHandler(ctx.channel())) {
                        transport.getHandler().handle(request, response);
                    }
                }
            } catch (TiesException e) {
                logger.error("Channel error: {}", e.getMessage(), e);
                ctx.channel().writeAndFlush(new CloseWebSocketFrame(1008, e.getMessage()));
            }
        } else if (inboundFrame instanceof CloseWebSocketFrame) {
            logger.trace("{} close requested by client", ctx.channel());
            try {
                ctx.close().await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("Channel close error: {}", e.getMessage(), e);
            }
        } else {
            logger.error("Unsupported frame type: {}", inboundFrame.getClass().getName());
            ctx.channel().writeAndFlush(new CloseWebSocketFrame(1003, "Only Binary Web Socket frames are supported"));
        }
    }

    protected void channelWrite0(Channel ch, TiesServiceScopeConsumer consumer) {
        try {
            try (WebSocketOutputHandler response = new WebSocketOutputHandler(ch)) {
                transport.getHandler().handle(consumer, response);
            }
        } catch (Exception e) {
            logger.error("Channel error: {}", e.getMessage(), e);
        }
    }
}
