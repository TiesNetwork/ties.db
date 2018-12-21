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

import static network.tiesdb.util.Safecheck.nullsafe;

import network.tiesdb.api.TiesVersion;
import network.tiesdb.handler.api.TiesHandler;
import network.tiesdb.service.api.TiesService;
import network.tiesdb.transport.api.TiesTransport;

/**
 * TiesDB WebSock transport implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesTransportImpl implements TiesTransport {

    private static final TiesTransportImplVersion IMPLEMENTATION_VERSION = TiesTransportImplVersion.v_0_0_1_prealpha;

    private final TiesTransportConfigImpl config;
    private final TiesHandler handler;

    public TiesTransportImpl(TiesService tiesService, TiesTransportConfigImpl config) {
        if (null == tiesService) {
            throw new NullPointerException("The tiesService should not be null");
        }
        if (null == config) {
            throw new NullPointerException("The config should not be null");
        }
        this.handler = nullsafe(nullsafe(config.getHandlerConfig()).getTiesHandlerFactory()).createHandler(tiesService);
        this.config = config;
    }

    public TiesTransportConfigImpl getTiesTransportConfig() {
        return config;
    }

    @Override
    public TiesVersion getVersion() {
        return IMPLEMENTATION_VERSION;
    }

    public TiesHandler getHandler() {
        return handler;
    }

}
