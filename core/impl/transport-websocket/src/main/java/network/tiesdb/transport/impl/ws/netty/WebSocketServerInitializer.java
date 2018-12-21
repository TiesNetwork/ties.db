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

import static network.tiesdb.util.Safecheck.nullsafe;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import network.tiesdb.context.api.TiesTransportConfig;
import network.tiesdb.exception.util.MessageHelper;
import network.tiesdb.transport.impl.ws.TiesTransportConfigImpl;
import network.tiesdb.transport.impl.ws.TiesTransportImpl;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServerInitializer.class);

    private static final String WEBSOCKET_PATH = "/websocket";

    private static class ConfigurableIdleStateHandler extends IdleStateHandler {
        private ConfigurableIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
            super(readerIdleTime, writerIdleTime, allIdleTime, nullsafe(unit));
        }

        private ConfigurableIdleStateHandler(TiesTransportConfigImpl config) {
            this(nullsafe(config).getIdleReaderTime(), nullsafe(config).getIdleWriterTime(), nullsafe(config).getIdleTime(),
                    TimeUnit.valueOf(nullsafe(config).getIdleTimeUnit()));
        }

        @Override
        protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
            switch (evt.state()) {
            case ALL_IDLE:
            case WRITER_IDLE:
                ctx.channel().write(new PingWebSocketFrame());
                break;
            case READER_IDLE:
            default:
                ctx.close();
            }
            super.channelIdle(ctx, evt);
        }
    }

    private final SslContext sslCtx;

    private final TiesTransportImpl transport;

    public WebSocketServerInitializer(TiesTransportImpl transport, SslContext sslCtx) {
        this.transport = transport;
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        config1stStage(ch);

        ChannelPipeline pipeline = ch.pipeline();
        if (null != sslCtx) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
        pipeline.addLast(new WebSocketIndexPageHandler(WEBSOCKET_PATH));
        pipeline.addLast(new WebSocketFrameHandler(transport));

        config2ndStage(ch);
    }

    protected void config1stStage(SocketChannel ch) {
        SocketChannelConfig config = ch.config();
        config.setTcpNoDelay(true);
        config.setKeepAlive(true);

        configToS(ch);
    }

    protected void config2ndStage(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new ConfigurableIdleStateHandler(transport.getTiesTransportConfig()));
    }

    protected void configToS(SocketChannel ch) {
        TiesTransportConfig config = nullsafe(transport.getTiesTransportConfig());
        if (config instanceof TiesTransportConfigImpl) {
            TiesTransportConfigImpl extConfig = (TiesTransportConfigImpl) config;
            Integer tos = extConfig.getTypeOfService();
            if (null != tos) {
                ch.config().setTrafficClass(tos);
            }
        } else {
            logger.warn(MessageHelper.notFullyCompatible(config.getClass(), TiesTransportConfigImpl.class),
                    "Using default TypeOfService for websocket");
        }
    }
}
