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
package network.tiesdb.transport.impl.ws;

import static network.tiesdb.util.Safecheck.nullsafe;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import network.tiesdb.api.TiesVersion;
import network.tiesdb.context.api.TiesTransportConfig;
import network.tiesdb.exception.TiesException;
import network.tiesdb.handler.api.TiesHandler;
import network.tiesdb.service.api.TiesService;
import network.tiesdb.transport.api.TiesTransport;
import network.tiesdb.transport.impl.ws.netty.WebSocketServer;

/**
 * TiesDB WebSock transport implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public abstract class TiesTransportImpl implements TiesTransport {

	private static final TiesTransportImplVersion IMPLEMENTATION_VERSION = TiesTransportImplVersion.v_0_0_1_prealpha;

	private final TiesTransportConfig config;
	private final WebSocketServer server;
	private final TiesHandler handler;

	public TiesTransportImpl(TiesService tiesService, TiesTransportConfig config) {
		if (null == tiesService) {
			throw new NullPointerException("The tiesService should not be null");
		}
		if (null == config) {
			throw new NullPointerException("The config should not be null");
		}
		this.handler = nullsafe(nullsafe(config.getHandlerConfig()).getTiesHandlerFactory()).createHandler(tiesService);
		this.config = config;
		this.server = new WebSocketServer(this);
	}

	@Override
	public TiesTransportConfig getTiesTransportConfig() {
		return config;
	}

	protected void startInternal() throws TiesException {
		try {
			server.start();
		} catch (CertificateException | SSLException | InterruptedException e) {
			throw new TiesException("Can't start TiesDB Transport", e);
		}
	}

	protected void initInternal() throws TiesException {
		server.init();
	}

	protected void stopInternal() throws TiesException {
		server.stop();
	}

	@Override
	public TiesVersion getVersion() {
		return IMPLEMENTATION_VERSION;
	}

	@Override
	public TiesHandler getHandler() {
		return handler;
	}

}
