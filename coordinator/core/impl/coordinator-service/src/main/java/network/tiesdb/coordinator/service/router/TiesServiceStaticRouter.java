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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.exception.TiesException;
import network.tiesdb.router.api.TiesRouter;
import network.tiesdb.router.api.TiesRoutingException;
import network.tiesdb.service.api.TiesService;
import network.tiesdb.transport.api.TiesTransportClient;
import network.tiesdb.transport.api.TiesTransportFactory;

public class TiesServiceStaticRouter implements TiesRouter {

    public static class StaticNode implements Node {

        private final short networkId;
        private final String addressString;

        public StaticNode(short networkId, String addressString) {
            this.networkId = networkId;
            this.addressString = addressString.toLowerCase();
        }

        @Override
        public short getNodeNetwork() {
            return networkId;
        }

        @Override
        public String getAddressString() {
            return addressString;
        }

        public static StaticNode fromString(String nodeDescription) {
            String[] parts = nodeDescription.split("[xX]", 2);
            return new StaticNode(Short.parseShort(parts[0]), "0x" + parts[1]);
        }

        public static StaticNode fromNode(Node nodeObject) {
            return new StaticNode(nodeObject.getNodeNetwork(), nodeObject.getAddressString());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((addressString == null) ? 0 : addressString.hashCode());
            result = prime * result + networkId;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StaticNode other = (StaticNode) obj;
            if (addressString == null) {
                if (other.addressString != null)
                    return false;
            } else if (!addressString.equals(other.addressString))
                return false;
            if (networkId != other.networkId)
                return false;
            return true;
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(TiesServiceStaticRouter.class);

    private final TiesService service;
    private final TiesServiceStaticRouterConfigImpl config;

    private final Map<StaticNode, URI> nodeMap = new HashMap<>();
    private final Map<String, TiesTransportFactory> transports = new HashMap<>();
    private final Map<URI, TiesTransportClient> clientCache = new HashMap<>();

    public TiesServiceStaticRouter(TiesService service, TiesServiceStaticRouterConfigImpl config) {
        this.service = service;
        this.config = config;
    }

    @Override
    public TiesTransportClient getClient(Node node) throws TiesRoutingException {
        LOG.trace("Node: {}", node);
        URI uri = nodeMap.get(StaticNode.fromNode(node));

        TiesTransportClient client = clientCache.get(uri);
        if (null != client) {
            try {
                return client.check();
            } catch (TiesException e) {
                LOG.error("Client check failed for uri: {}", uri, e);
            }
        }

        TiesTransportFactory tf = transports.get(uri.getScheme());
        if (null == tf) {
            throw new TiesRoutingException("Unknown protocol " + uri.getScheme() + " for node " + node + " route " + uri);
        }
        try {
            client = tf.createTransportClient(service, uri);
            client = client.check();
            clientCache.put(uri, client);
            return client;
        } catch (TiesException e) {
            throw new TiesRoutingException("Can't connect Node " + node, e);
        }
    }

    @Override
    public void start() throws TiesException {
        // NOP
    }

    @Override
    public void stop() throws TiesException {
        this.nodeMap.clear();
        this.transports.clear();
        this.clientCache.values().forEach(client -> {
            try {
                client.stop();
            } catch (TiesException e) {
                LOG.error("Client stop failed: {}", client, e);
            }
        });
        this.clientCache.clear();
    }

    @Override
    public void init() throws TiesException {
        this.nodeMap.putAll(config.getNodesURIMap());
        this.transports.putAll(config.getTransportFactoriesMap());
    }

}
