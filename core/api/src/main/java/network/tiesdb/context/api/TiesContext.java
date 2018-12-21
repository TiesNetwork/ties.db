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
package network.tiesdb.context.api;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Context of TiesDB service.
 * 
 * <P>
 * Contains configuration of TiesDB service and service runtime state.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
// TODO change to map and allow usage of multiple contexts mappings
public class TiesContext {

    private Map<String, TiesServiceConfig> config;

    public Map<String, TiesServiceConfig> getConfig() {
        return config;
    }

    public void setConfig(Map<String, TiesServiceConfig> configs) {
        this.config = configs;
    }

    /**
     * Searches a suitable {@link TiesContextFactory}
     * 
     * @param contextTypeName - name of the context class
     * @return {@link TiesContextFactory} instance or null
     */
    public static TiesContextFactory getTiesContextFactory(String contextTypeName) {
        Iterator<TiesContextFactory> services = ServiceLoader.load(TiesContextFactory.class).iterator();
        while (services.hasNext()) {
            TiesContextFactory tiesContextService = services.next();
            if (tiesContextService.matchesContextType(contextTypeName)) {
                return tiesContextService;
            }
        }
        return null;
    }
}