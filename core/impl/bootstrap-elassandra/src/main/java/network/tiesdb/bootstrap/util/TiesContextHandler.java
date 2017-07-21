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
package network.tiesdb.bootstrap.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.context.api.TiesServiceConfig;
import network.tiesdb.context.api.TiesContext;
import network.tiesdb.context.api.TiesContextFactory;
import network.tiesdb.exception.TiesConfigurationException;

/**
 * TiesDB context wrapper.
 * 
 * <P>Contains some utility methods for TiesDB context handling.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesContextHandler {

	private static final Logger logger = LoggerFactory.getLogger(TiesContextHandler.class);

	public static interface TiesContextHandlerFactory {
		TiesContextHandler createHandler(TiesContextFactory contextFactory, URL contextUrl)
				throws TiesConfigurationException;
	}

	private static TiesContextHandlerFactory factory = new TiesContextHandlerFactory() {
		@Override
		public TiesContextHandler createHandler(TiesContextFactory contextFactory, URL contextUrl)
				throws TiesConfigurationException {
			return new TiesContextHandler(contextFactory, contextUrl);
		}
	};

	public static TiesContextHandlerFactory getFactory() {
		return factory;
	}

	public static void setFactory(TiesContextHandlerFactory newFactory) {
		if (null == newFactory) {
			throw new NullPointerException("The newFactory should not be null");
		}
		factory = newFactory;
	}

	private final TiesContext context;

	protected TiesContextHandler(TiesContextFactory contextService, URL contextUrl) throws TiesConfigurationException {
		this(initContext(contextService, contextUrl));
	}

	public TiesContextHandler(TiesContext context) {
		if (null == context) {
			throw new NullPointerException("The context should not be null");
		}
		this.context = context;
	}

	public TiesServiceConfig getConfig(String name) {
		return context.getConfig().get(name);
	}

	protected static TiesContext initContext(TiesContextFactory contextService, URL contextUrl)
			throws TiesConfigurationException {
		if (null == contextService) {
			throw new NullPointerException("The contextService should not be null");
		}
		if (null == contextUrl) {
			throw new NullPointerException("The contextUrl should not be null");
		}
		TiesContext context = null;
		try (InputStream is = contextUrl.openStream()) {
			context = contextService.readContext(is);
		} catch (IOException e) {
			throw new TiesConfigurationException("Context initialization failed", e);
		}
		if (null == context) {
			throw new TiesConfigurationException("TiesDB Service settings could not be read from " + contextUrl);
		}
		logger.debug("TiesDB Service settings read from {}", contextUrl);
		if (null == context.getConfig() || context.getConfig().isEmpty()) {
			throw new TiesConfigurationException("TiesDB Service settings configuration is missing");
		}
		return context;
	}

	public Set<String> getConfigsNames() {
		return context.getConfig().keySet();
	}

	public TiesContext getDelegate() {
		return context;
	}
}
