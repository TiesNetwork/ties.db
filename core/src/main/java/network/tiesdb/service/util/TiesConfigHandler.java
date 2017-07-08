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
package network.tiesdb.service.util;

import java.util.List;

import network.tiesdb.context.api.TiesServiceConfig;
import network.tiesdb.context.api.TiesTransportConfig;
import network.tiesdb.service.api.TiesServiceFactory;

/**
 * TiesDB configuration wrapper.
 * 
 * <P>Contains some utility methods for TiesDB configuration handling.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesConfigHandler implements TiesServiceConfig {

	protected final TiesServiceConfig config;

	public static interface TiesConfigHandlerFactory {
		TiesConfigHandler createHandler(TiesServiceConfig config);
	}

	private static TiesConfigHandlerFactory factory = new TiesConfigHandlerFactory() {
		@Override
		public TiesConfigHandler createHandler(TiesServiceConfig config) {
			return new TiesConfigHandler(config);
		}
	};

	public static TiesConfigHandlerFactory getFactory() {
		return factory;
	}

	public static void setFactory(TiesConfigHandlerFactory newFactory) {
		if (newFactory == null) {
			throw new NullPointerException("The newFactory should not be null");
		}
		factory = newFactory;
	}

	protected TiesConfigHandler(TiesServiceConfig config) {
		if (config == null) {
			throw new NullPointerException("The config should not be null");
		}
		this.config = config;
	}

	public boolean isServiceStopCritical() {
		return config.isServiceStopCritical();
	}

	public void setServiceStopCritical(boolean serviceStopCritical) {
		config.setServiceStopCritical(serviceStopCritical);
	}

	public List<TiesTransportConfig> getTransports() {
		return config.getTransports();
	}

	public void setTransport(List<TiesTransportConfig> transports) {
		config.setTransports(transports);
	}

	public TiesServiceFactory getTiesServiceFactory() {
		return config.getTiesServiceFactory();
	}

	public void setTransports(List<TiesTransportConfig> transports) {
		config.setTransports(transports);
	}

	public TiesServiceConfig getDelegate() {
		return config;
	}

}
