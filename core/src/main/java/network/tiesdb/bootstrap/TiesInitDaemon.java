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
package network.tiesdb.bootstrap;

import java.io.File;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.context.api.TiesContext.TiesConfig;
import network.tiesdb.context.api.TiesContextFactory;
import network.tiesdb.context.impl.yaml.YAMLContextFactory;
import network.tiesdb.service.api.TiesServiceDaemon;
import network.tiesdb.service.api.TiesServiceFactory;
import network.tiesdb.service.util.TiesContextHandler;

/**
 * TiesDB initialization daemon.
 * 
 * <P>Main logic for TiesDB service initialization outside of the main thread.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesInitDaemon implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(TiesInitDaemon.class);

	public static final String DEFAULT_CONFIG_FILE_NAME = "tiesdb.yml";
	private static final String DEFAULT_CONFIG_CONTEXT_FACTORY = YAMLContextFactory.class.getName();

	private Throwable initializationException;

	@Override
	public void run() {
		init();
	}

	protected void init() {
		logger.trace("Starting TiesDB initialization process...");
		try {
			logger.trace("Loading TiesDB context...");
			TiesContextHandler context = TiesContextHandler.getFactory().createHandler(
					TiesContextFactory.getTiesContextFactory(DEFAULT_CONFIG_CONTEXT_FACTORY),
					new File(getConfigDir() + File.separator + DEFAULT_CONFIG_FILE_NAME).toURI().toURL());
			logger.trace("TiesDB context loaded successfully...");
			for (Entry<String, TiesConfig> entry : context.getConfig().entrySet()) {
				String name = entry.getKey();
				logger.trace("Found TiesDB service \"{}\" configuration", name);
				TiesConfig config = entry.getValue();
				TiesServiceFactory tiesServiceFactory = TiesServiceFactory
						.getTiesServiceFactory(config.getServiceVersion());
				if (tiesServiceFactory == null) {
					logger.error("Can't find TiesServiceFactory for service \"{}\"", name);
				} else {
					logger.trace("Launching TiesDB service \"{}\"", name);
					try {
						TiesServiceDaemon service = tiesServiceFactory.createServiceDaemon(name, config);
						service.init();
						service.start();
						logger.info("TiesDB service \"{}\" launched successfully", name);
					} catch (Throwable e) {
						logger.error("TiesDB service \"{}\" launch failed", name, e);
					}
				}
			}
		} catch (Throwable e) {
			logger.error("An unrecoverable error occurred during the TiesDB initialization process", e);
			initializationException = e;
		}
		logger.trace("TiesDB initialization process compleated successfully...");
	}

	public Throwable getInitializationException() {
		return initializationException;
	}

	private static String getConfigDir() {
		String cassandra_conf = System.getenv("CASSANDRA_CONF");
		if (cassandra_conf == null) {
			cassandra_conf = System.getProperty("cassandra.conf",
					System.getProperty("path.conf", getHomeDir() + File.separator + "conf"));
		}
		return cassandra_conf;
	}

	private static String getHomeDir() {
		String cassandra_home = System.getenv("CASSANDRA_HOME");
		if (cassandra_home == null) {
			cassandra_home = System.getProperty("cassandra.home", System.getProperty("path.home"));
			if (cassandra_home == null)
				throw new IllegalStateException(
						"Cannot start, environnement variable CASSANDRA_HOME and system properties cassandra.home"
								+ " or path.home are null. Please set one of these to start properly");
		}
		return cassandra_home;
	}

}
