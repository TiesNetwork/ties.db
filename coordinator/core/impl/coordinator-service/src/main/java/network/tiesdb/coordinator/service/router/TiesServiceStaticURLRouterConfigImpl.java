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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.json.JSONTokener;

import network.tiesdb.context.api.annotation.TiesConfigElement;
import network.tiesdb.coordinator.service.router.TiesServiceStaticRouter.StaticNode;
import network.tiesdb.router.api.TiesRouterFactory;

/**
 * TiesDB Coordinator service configuration implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
@TiesConfigElement({ TiesServiceStaticURLRouterConfigImpl.BINDING, TiesServiceStaticURLRouterConfigImpl.SHORT_BINDING })
public class TiesServiceStaticURLRouterConfigImpl extends TiesServiceStaticRouterConfigImpl {

    static final String BINDING = "network.tiesdb.service.router.StaticURLRouter";
    static final String SHORT_BINDING = "TiesDBStaticURLRouter";

    public TiesServiceStaticURLRouterConfigImpl() {
        // NOP Is not empty config values
    }

    public TiesServiceStaticURLRouterConfigImpl(String value) {
        // NOP If this constructor is called then config values is empty and we
        // should use default
    }

    @Override
    public TiesRouterFactory getTiesRouterFactory() {
        return new TiesServiceStaticRouterFactoryImpl(this);
    }

    public void setNodesUrl(String nodesUrlString) {
        try {
            URL nodesUrl = new URL(nodesUrlString);
            URLConnection connection = nodesUrl.openConnection();
            String agentName = "TiesDB@" + System.getProperty("os.name").replace(' ', '_') + "_" + System.getProperty("os.arch");
            System.out.println("User-Agent: " + agentName);
            connection.setRequestProperty("User-Agent", agentName);
            try {
                connection.connect();
                JSONTokener jsonTokener = new JSONTokener(connection.getInputStream());
                JSONObject jsonObject = new JSONObject(jsonTokener);
                Map<StaticNode, URI> newNodes = new HashMap<>(jsonObject.length());
                updateNodesURIMap(newNodes);
                Iterator<String> it = jsonObject.keys();
                while (it.hasNext()) {
                    String k = it.next();
                    newNodes.put(StaticNode.fromString(k), URI.create(jsonObject.getString(k)));
                }
                setNodesURIMap(newNodes);
            } catch (Exception e) {
                HttpsURLConnection urlconnection = (HttpsURLConnection) connection;
                if (connection instanceof HttpsURLConnection) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(urlconnection.getErrorStream()))) {
                        System.err.println(br.lines().collect(Collectors.joining(System.lineSeparator())));
                    }
                }
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load node adresses from url: " + nodesUrlString, e);
        }
    }

}
