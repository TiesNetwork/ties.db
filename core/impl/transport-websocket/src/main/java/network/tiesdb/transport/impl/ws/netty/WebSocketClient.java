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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import network.tiesdb.exception.TiesException;
import network.tiesdb.service.scope.api.TiesServiceScopeConsumer;
import network.tiesdb.transport.impl.ws.TiesTransportImpl;

import java.io.IOException;
import java.net.URI;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class WebSocketClient {

    private static final EventLoopGroup group = new NioEventLoopGroup();

    private final URI destination;
    private final WebSocketFrameHandler frameHandler;

    private Channel ch;

    public WebSocketClient(TiesTransportImpl transport, URI destination) {
        this.frameHandler = new WebSocketFrameHandler(transport);
        this.destination = destination;
    }

    public void open() throws InterruptedException {
        String protocol = destination.getScheme();
        if (!"ws".equals(protocol)) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
        // If you change it to V00, ping is not supported and remember to change
        // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
        final WebSocketClientHandler handler = new WebSocketClientHandler(WebSocketClientHandshakerFactory.newHandshaker(destination,
                WebSocketVersion.V13, null, false, HttpHeaders.EMPTY_HEADERS, 1280000), frameHandler);

        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("http-codec", new HttpClientCodec());
                pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                pipeline.addLast("ws-handler", handler);
            }
        });

        // System.out.println("WebSocket Client connecting");
        ch = b.connect(destination.getHost(), destination.getPort()).sync().channel();
        handler.handshakeFuture().sync();
    }

    public void close() throws InterruptedException {
        // System.out.println("WebSocket Client sending close");
        ch.writeAndFlush(new CloseWebSocketFrame());
        ch.closeFuture().sync();
        ch = null;
        // group.shutdownGracefully();
    }

    public void eval(final String text) throws IOException {
        ch.writeAndFlush(new TextWebSocketFrame(text));
    }

    public void init() {
        // NOP
    }

    public void request(TiesServiceScopeConsumer consumer) throws Exception {
        frameHandler.channelWrite0(ch, consumer);
    }

    public void check() throws TiesException {
        if (null != ch) {
            if (ch.isActive()) {
                return;
            }
            if (ch.isOpen()) {
                return;
            }
        }
        try {
            init();
            open();
        } catch (InterruptedException e) {
            throw new TiesException("Client check failed", e);
        }
    }
}