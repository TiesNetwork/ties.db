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

import network.tiesdb.api.TiesHandler;
import network.tiesdb.api.TiesRequest;
import network.tiesdb.api.TiesResponse;
import network.tiesdb.api.TiesService;
import network.tiesdb.api.TiesTransport;
import network.tiesdb.context.api.TiesTransportConfig;
import network.tiesdb.exception.TiesException;
import network.tiesdb.transport.impl.ws.netty.WebSocketServer;

/**
 * TiesDB WebSock transport implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesTransportImpl implements TiesTransport {

	private final TiesTransportConfigImpl config;
	private final WebSocketServer server;
	private final TiesHandler handler;

	public TiesTransportImpl(TiesService tiesService, TiesTransportConfig config) {
		this.handler = nullsafe(nullsafe(tiesService, "The tiesService should not be null").getHandler(),"The tiesService returned null on getRawHandler");
		this.config = (TiesTransportConfigImpl) nullsafe(config, "The config should not be null");
		this.server = new WebSocketServer(this);
	}

	@Override
	public TiesTransportConfigImpl getTiesTransportConfig() {
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
	public void handle(TiesRequest request, TiesResponse response) {
		this.handler.handle(request, response);
	}

}
