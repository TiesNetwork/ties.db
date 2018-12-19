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
package network.tiesdb.transport.impl.ws;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import network.tiesdb.exception.TiesConfigurationException;
import network.tiesdb.exception.TiesException;
import network.tiesdb.transport.api.TiesTransport;
import network.tiesdb.transport.api.TiesTransportServer;
import network.tiesdb.transport.impl.ws.netty.WebSocketServer;

/**
 * TiesDB WebSock transport daemon implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesTransportServerImpl implements TiesTransportServer {

    private final WebSocketServer server;

    public TiesTransportServerImpl(TiesTransportImpl transport) throws TiesConfigurationException {
        this.server = new WebSocketServer(transport);
    }

    @Override
    public void init() throws TiesException {
        server.init();
    }

    @Override
    public void start() throws TiesException {
        try {
            server.start();
        } catch (CertificateException | SSLException | InterruptedException e) {
            throw new TiesException("Can't start TiesDB Transport daemon", e);
        }
    }

    @Override
    public void stop() throws TiesException {
        server.stop();
    }

}
