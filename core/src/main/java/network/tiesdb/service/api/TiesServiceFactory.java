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
package network.tiesdb.service.api;

import java.util.Iterator;
import java.util.ServiceLoader;

import network.tiesdb.context.api.TiesContext.TiesConfig;
import network.tiesdb.exception.TiesConfigurationException;

/**
 * TiesDB service factory.
 * 
 * <P>Factory of TiesDB services.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public interface TiesServiceFactory {

	static TiesServiceFactory getTiesServiceFactory(String serviceVersion) {
		Iterator<TiesServiceFactory> factories = ServiceLoader.load(TiesServiceFactory.class).iterator();
		while (factories.hasNext()) {
			TiesServiceFactory factory = factories.next();
			if (factory.matchesServiceVersion(serviceVersion)) {
				return factory;
			}
		}
		return null;
	}

	TiesServiceDaemon createServiceDaemon(String name, TiesConfig config) throws TiesConfigurationException;

	boolean matchesServiceVersion(String version);

}
