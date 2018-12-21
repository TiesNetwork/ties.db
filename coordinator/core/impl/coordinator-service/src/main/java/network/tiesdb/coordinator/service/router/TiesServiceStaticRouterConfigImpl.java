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
package network.tiesdb.coordinator.service.router;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import network.tiesdb.context.api.TiesRouterConfig;
import network.tiesdb.context.api.TiesTransportConfig;
import network.tiesdb.context.api.annotation.TiesConfigElement;
import network.tiesdb.coordinator.service.router.TiesServiceStaticRouter.StaticNode;
import network.tiesdb.router.api.TiesRouterFactory;
import network.tiesdb.transport.api.TiesTransportFactory;

/**
 * TiesDB Coordinator service configuration implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
@TiesConfigElement({ TiesServiceStaticRouterConfigImpl.BINDING, TiesServiceStaticRouterConfigImpl.SHORT_BINDING })
public class TiesServiceStaticRouterConfigImpl implements TiesRouterConfig {

    static final String BINDING = "network.tiesdb.service.router.StaticRouter";
    static final String SHORT_BINDING = "TiesDBStaticRouter";

    private Map<StaticNode, URI> nodeAddresses;
    private Map<String, TiesTransportFactory> transports;

    public TiesServiceStaticRouterConfigImpl() {
        // NOP Is not empty config values
    }

    public TiesServiceStaticRouterConfigImpl(String value) {
        // NOP If this constructor is called then config values is empty and we
        // should use default
    }

    @Override
    public TiesRouterFactory getTiesRouterFactory() {
        return new TiesServiceStaticRouterFactoryImpl(this);
    }

    public Map<StaticNode, URI> getNodesURIMap() {
        return nodeAddresses;
    }

    public void setNodes(Map<String, String> nodes) {
        Map<StaticNode, URI> newNodes = new HashMap<>(nodes.size());
        nodes.forEach((k, v) -> {
            newNodes.put(StaticNode.fromString(k), URI.create(v));
        });
        this.nodeAddresses = Collections.unmodifiableMap(newNodes);
    }

    public Map<String, TiesTransportFactory> getTransportFactoriesMap() {
        return transports;
    }

    public void setTransports(Map<String, TiesTransportConfig> transports) {
        Map<String, TiesTransportFactory> newTransports = new HashMap<>(transports.size());
        transports.forEach((k, v) -> {
            newTransports.put(k, v.getTiesTransportFactory());
        });
        this.transports = Collections.unmodifiableMap(newTransports);
    }

}
