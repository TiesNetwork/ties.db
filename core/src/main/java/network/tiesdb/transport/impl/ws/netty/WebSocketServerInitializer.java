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
import network.tiesdb.api.TiesTransport;
import network.tiesdb.context.api.TiesTransportConfig;
import network.tiesdb.exception.util.MessageHelper;
import network.tiesdb.service.impl.TiesServiceConfigImpl;
import network.tiesdb.transport.impl.ws.TiesTransportConfigImpl;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketServerInitializer.class);

	private static final String WEBSOCKET_PATH = "/websocket";

	private static class ConfigurableIdleStateHandler extends IdleStateHandler {
		private ConfigurableIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime,
				TimeUnit unit) {
			super(readerIdleTime, writerIdleTime, allIdleTime, nullsafe(unit));
		}

		private ConfigurableIdleStateHandler(TiesTransportConfigImpl config) {
			this(nullsafe(config).getIdleReaderTime(), nullsafe(config).getIdleWriterTime(),
					nullsafe(config).getIdleTime(), TimeUnit.valueOf(nullsafe(config).getIdleTimeUnit()));
		}

		private ConfigurableIdleStateHandler(TiesTransportConfig config) {
			this((TiesTransportConfigImpl) config);
		}

		@Override
		protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
			switch (evt.state()) {
			case WRITER_IDLE:
				ctx.channel().write(new PingWebSocketFrame());
				break;
			default:
				ctx.close();
			}
			super.channelIdle(ctx, evt);
		}
	}

	private final SslContext sslCtx;

	private final TiesTransport transport;

	public WebSocketServerInitializer(TiesTransport transport, SslContext sslCtx) {
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
		pipeline.addLast(new ConfigurableIdleStateHandler(nullsafe(transport.getTiesTransportConfig())));
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
			logger.warn(MessageHelper.notFullyCompatible(config.getClass(), TiesServiceConfigImpl.class),
					"Using default TypeOfService for websocket");
		}
	}
}
