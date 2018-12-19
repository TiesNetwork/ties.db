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

import java.net.URI;

import network.tiesdb.exception.TiesConfigurationException;
import network.tiesdb.exception.TiesException;
import network.tiesdb.service.api.TiesService;
import network.tiesdb.service.scope.api.TiesServiceScopeConsumer;
import network.tiesdb.transport.api.TiesTransport;
import network.tiesdb.transport.api.TiesTransportClient;
import network.tiesdb.transport.impl.ws.netty.WebSocketClient;

/**
 * TiesDB WebSock transport daemon implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesTransportClientImpl implements TiesTransportClient {

    private final WebSocketClient client;

    public TiesTransportClientImpl(TiesTransportImpl transport, URI uri) throws TiesConfigurationException {
        this.client = new WebSocketClient(transport, uri);
    }

    @Override
    public void init() throws TiesException {
        client.init();
    }

    @Override
    public void start() throws TiesException {
        try {
            client.open();
        } catch (InterruptedException e) {
            throw new TiesException("Can't start TiesDB Transport client", e);
        }
    }

    @Override
    public void stop() throws TiesException {
        try {
            client.close();
        } catch (InterruptedException e) {
            throw new TiesException("Can't stop TiesDB Transport client", e);
        }
    }

    @Override
    public void request(TiesServiceScopeConsumer consumer) throws TiesException {
        try {
            client.request(consumer);
        } catch (Exception e) {
            throw new TiesException("Request failed", e);
        }
    }

    @Override
    public TiesTransportClient check() throws TiesException {
        client.check();
        return this;
    }

}
