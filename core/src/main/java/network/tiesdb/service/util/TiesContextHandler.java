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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.context.api.TiesContext;
import network.tiesdb.context.api.TiesContext.TiesConfig;
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
		if (newFactory == null) {
			throw new NullPointerException("The newFactory should not be null");
		}
		factory = newFactory;
	}

	private final TiesContextFactory contextService;
	private final URL contextUrl;
	private final TiesContext context;

	protected TiesContextHandler(TiesContextFactory contextService, URL contextUrl) throws TiesConfigurationException {
		this.context = initContext(contextService, contextUrl);
		this.contextService = contextService;
		this.contextUrl = contextUrl;
	}

	public Map<String, TiesConfig> getConfig() {
		return context.getConfig();
	}

	protected static TiesContext initContext(TiesContextFactory contextService, URL contextUrl)
			throws TiesConfigurationException {
		if (contextService == null) {
			throw new NullPointerException("The contextService should not be null");
		}
		if (contextUrl == null) {
			throw new NullPointerException("The contextUrl should not be null");
		}
		try (InputStream is = contextUrl.openStream()) {
			TiesContext context = contextService.readContext(is);
			if (context == null) {
				logger.warn("TiesDB Service settings culd not be read from {}, default settings will be used",
						contextUrl);
				context = new TiesContext();
			} else {
				logger.debug("TiesDB Service settings read from {}", contextUrl);
			}
			return context;
		} catch (IOException e) {
			throw new TiesConfigurationException("Context initialization failed", e);
		}
	}

	public void save() throws TiesConfigurationException {
		try (OutputStream fos = new FileOutputStream(new File(contextUrl.toURI()))) {
			contextService.writeContext(fos, context);
		} catch (IOException | URISyntaxException | TiesConfigurationException e) {
			throw new TiesConfigurationException("Context was not saved", e);
		}
	}
}
