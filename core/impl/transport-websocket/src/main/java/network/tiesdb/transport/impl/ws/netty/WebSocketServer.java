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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import network.tiesdb.context.api.TiesTransportConfig;
import network.tiesdb.exception.util.MessageHelper;
import network.tiesdb.transport.impl.ws.TiesTransportConfigImpl;
import network.tiesdb.transport.impl.ws.TiesTransportImpl;

/**
 * A HTTP server which serves Web Socket requests at:
 *
 * http://localhost:8080/websocket
 *
 * Open your browser at
 * <a href="http://localhost:8080/">http://localhost:8080/</a>, then the demo
 * page will be loaded and a Web Socket connection will be made automatically.
 *
 * This server illustrates support for the different web socket specification
 * versions and will work with:
 *
 * <ul>
 * <li>Safari 5+ (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 6-13 (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 14+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Chrome 16+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * <li>Firefox 7+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Firefox 11+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * </ul>
 */
public class WebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    private final TiesTransportImpl transport;
    private AtomicReference<SafeConfig> configRef = new AtomicReference<>();
    private AtomicReference<ChannelGroupManager> managerRef = new AtomicReference<>();

    private static final class SafeConfig {

        private final Integer port;
        private final String host;
        private final boolean secured;
        private final Integer workerThreadsCount;
        private final Integer acceptorThreadsCount;

        private SafeConfig(TiesTransportConfig config) {
            if (null == config) {
                throw new NullPointerException("The configRef should not be null");
            }
            this.port = config.getServicePort();
            this.host = config.getServiceAddress();
            if (config instanceof TiesTransportConfigImpl) {
                TiesTransportConfigImpl extConfig = (TiesTransportConfigImpl) config;
                this.secured = nullsafe(extConfig.getSecurity()).isSecuredSocket();
                this.workerThreadsCount = nullsafe(extConfig.getWorkerThreadsCount());
                this.acceptorThreadsCount = nullsafe(extConfig.getAcceptorThreadsCount());
            } else {
                logger.warn(MessageHelper.notFullyCompatible(config.getClass(), TiesTransportConfigImpl.class),
                        "Using default settings for missing elements");
                this.secured = false;
                this.workerThreadsCount = 5;
                this.acceptorThreadsCount = 1;
            }
        }
    }

    private static final class ChannelGroupManager {

        private final AtomicReference<Channel> chRef = new AtomicReference<>();
        private final NioEventLoopGroup bossGroup;
        private final NioEventLoopGroup workerGroup;

        private ChannelGroupManager(NioEventLoopGroup bossGroup, NioEventLoopGroup workerGroup) {
            if (null == workerGroup) {
                throw new NullPointerException("The workerGroup should not be null");
            }
            if (null == bossGroup) {
                throw new NullPointerException("The bossGroup should not be null");
            }
            this.bossGroup = bossGroup;
            this.workerGroup = workerGroup;
        }

        private void stop() {
            Channel ch = chRef.getAndSet(null);
            try {
                if (null != ch && ch.isOpen()) {
                    ch.close();
                    ch.closeFuture().sync();
                } else {
                    logger.warn("Channel is null or not opened {}", ch.localAddress());
                }
            } catch (InterruptedException e) {
                logger.error("Can't close web socket channel {}", ch.localAddress(), e);
            } finally {
                if (bossGroup != null) {
                    try {
                        bossGroup.shutdownGracefully().sync();
                    } catch (InterruptedException e) {
                        logger.error("Can't stop channel acceptor group", e);
                    }
                } else {
                    logger.trace("No channel acceptor group");
                }
                if (null != workerGroup) {
                    try {
                        workerGroup.shutdownGracefully().sync();
                    } catch (InterruptedException e) {
                        logger.error("Can't stop channel worker group", e);
                    }
                } else {
                    logger.trace("No channel worker group");
                }
            }
        }
    }

    public WebSocketServer(TiesTransportImpl transport) {
        if (null == transport) {
            throw new NullPointerException("The transport should not be null");
        }
        this.transport = transport;
    }

    public void init() {
        if (!configRef.compareAndSet(null, new SafeConfig(transport.getTiesTransportConfig()))) {
            throw new IllegalStateException(this + " has already been initialized");
        }
    }

    public void stop() {
        ChannelGroupManager manager = this.managerRef.getAndSet(null);
        if (manager != null) {
            manager.stop();
        }
    }

    public void start() throws CertificateException, SSLException, InterruptedException {

        SafeConfig config = configRef.get();
        if (null == config) {
            throw new NullPointerException("The config should not be null");
        }

        ChannelGroupManager manager = new ChannelGroupManager(new NioEventLoopGroup(config.acceptorThreadsCount),
                new NioEventLoopGroup(config.workerThreadsCount));

        try {
            if (!managerRef.compareAndSet(null, manager)) {
                throw new IllegalStateException(ChannelGroupManager.class.getSimpleName() + " was already created");
            }

            final SslContext sslCtx;
            if (config.secured) {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            } else {
                sslCtx = null;
            }

            ServerBootstrap b = new ServerBootstrap();
            b.group(manager.bossGroup, manager.workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.handler(new LoggingHandler(getLoggerLogLevel()));
            b.childHandler(new WebSocketServerInitializer(transport, sslCtx));

            Channel ch = b.bind(config.host, config.port).sync().channel();
            if (!manager.chRef.compareAndSet(null, ch)) {
                throw new IllegalStateException(Channel.class.getSimpleName() + " was already bound");
            }

            String addressString = ch.localAddress().toString();
            if (ch.localAddress() instanceof InetSocketAddress) {
                String realHost = config.host;
                Integer realPort = config.port;
                InetSocketAddress sockAddress = (InetSocketAddress) ch.localAddress();
                realPort = sockAddress.getPort();
                InetAddress address = sockAddress.getAddress();
                if (address.isAnyLocalAddress()) {
                    realHost = InetAddress.getLoopbackAddress().getHostAddress();
                } else {
                    realHost = address.getHostAddress();
                }
                addressString = realHost + ":" + realPort;
            }

            System.out.println("Web socket available at " + (config.secured ? "wss" : "ws") + "://" + addressString);
        } catch (Throwable e) {
            logger.debug("Can't start server", e);
            manager.stop();
            throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e.getMessage(), e));
        }
    }

    private static LogLevel getLoggerLogLevel() {
        return logger.isTraceEnabled() ? LogLevel.TRACE
                : logger.isDebugEnabled() ? LogLevel.DEBUG
                        : logger.isInfoEnabled() ? LogLevel.INFO : logger.isWarnEnabled() ? LogLevel.WARN : LogLevel.ERROR;
    }
}
