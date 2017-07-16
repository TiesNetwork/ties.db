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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.context.api.TiesServiceConfig;
import network.tiesdb.bootstrap.util.TiesContextHandler;
import network.tiesdb.context.api.TiesContext;
import network.tiesdb.context.impl.yaml.YAMLContextFactory;
import network.tiesdb.exception.TiesStartupException;
import network.tiesdb.service.TiesDaemon;
import network.tiesdb.service.api.TiesServiceDaemon;
import network.tiesdb.service.api.TiesServiceFactory;

/**
 * TiesDB initialization.
 * 
 * <P>Main logic for TiesDB service initialization outside of the main thread.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesInitialization implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(TiesInitialization.class);

	public static final String DEFAULT_CONFIG_FILE_NAME = "tiesdb.yml";
	private static final String DEFAULT_CONFIG_CONTEXT_FACTORY = YAMLContextFactory.class.getName();

	@Override
	public void run() {
		logger.trace("Starting TiesDB boot sequence...");
		try {
			initialize();
		} catch (TiesStartupException e) {
			logger.error("An unrecoverable error occurred during the TiesDB initialization process", e);
			System.exit(e.getExitCode());
		} catch (Throwable e) {
			logger.error("An unexpected error occurred during the TiesDB initialization process", e);
			System.exit(-2);
		}
		logger.trace("Boot sequence of TiesDB finished successfully");
	}

	public void initialize() throws Throwable {
		logger.trace("Starting TiesDB initialization process...");
		logger.trace("Loading TiesDB context...");
		TiesContextHandler contextHandler = TiesContextHandler.getFactory().createHandler(
				TiesContext.getTiesContextFactory(DEFAULT_CONFIG_CONTEXT_FACTORY),
				new File(getConfigDir() + File.separator + DEFAULT_CONFIG_FILE_NAME).toURI().toURL());
		logger.trace("TiesDB context loaded successfully...");
		if (!TiesDaemon.instance.context.compareAndSet(null, contextHandler.getDelegate())) {
			throw new IllegalStateException("TiesDaemon context was initialized already");
		}
		logger.trace("TiesDB context contains {} services", contextHandler.getConfigsNames().size());
		for (String name : contextHandler.getConfigsNames()) {
			logger.trace("Found TiesDB service \"{}\" configuration", name);
			TiesServiceConfig config = contextHandler.getConfig(name);
			TiesServiceFactory tiesServiceFactory = config.getTiesServiceFactory();
			if (null == tiesServiceFactory) {
				logger.error("Can't find TiesServiceFactory for service \"{}\"", name);
			} else {
				logger.trace("Launching TiesDB service \"{}\"", name);
				try {
					TiesServiceDaemon service = tiesServiceFactory.createServiceDaemon(name, config);
					service.init();
					TiesShutdown.addShutdownHook(service);
					service.start();
					logger.info("TiesDB service \"{}\" launched successfully", name);
				} catch (Throwable e) {
					logger.error("TiesDB service \"{}\" launch failed", name, e);
				}
			}
		}
		logger.trace("TiesDB initialization process compleated successfully...");
	}

	private static String getConfigDir() {
		String cassandra_conf = System.getenv("CASSANDRA_CONF");
		if (null == cassandra_conf) {
			cassandra_conf = System.getProperty("cassandra.conf",
					System.getProperty("path.conf", getHomeDir() + File.separator + "conf"));
		}
		return cassandra_conf;
	}

	private static String getHomeDir() {
		String cassandra_home = System.getenv("CASSANDRA_HOME");
		if (null == cassandra_home) {
			cassandra_home = System.getProperty("cassandra.home", System.getProperty("path.home"));
			if (null == cassandra_home)
				throw new IllegalStateException(
						"Cannot start, environnement variable CASSANDRA_HOME and system properties cassandra.home"
								+ " or path.home are null. Please set one of these to start properly");
		}
		return cassandra_home;
	}

}
