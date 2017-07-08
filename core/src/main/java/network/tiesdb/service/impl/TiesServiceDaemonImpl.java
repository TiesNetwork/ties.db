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
package network.tiesdb.service.impl;

import org.apache.cassandra.service.MigrationListener;
import org.apache.cassandra.service.MigrationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.api.TiesService;
import network.tiesdb.context.api.TiesServiceConfig;
import network.tiesdb.exception.TiesConfigurationException;
import network.tiesdb.exception.TiesException;
import network.tiesdb.service.api.TiesServiceDaemon;
import network.tiesdb.service.util.TiesConfigHandler;

/**
 * TiesDB basic service daemon implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesServiceDaemonImpl extends TiesServiceImpl implements Runnable, TiesServiceDaemon {

	private static final Logger logger = LoggerFactory.getLogger(TiesServiceDaemonImpl.class);

	private final Thread tiesServiceThread;

	private String name;

	// private WebSocketServer webSocketServer;

	public TiesServiceDaemonImpl(String name, TiesServiceConfig config) throws TiesConfigurationException {
		super(TiesConfigHandler.getFactory().createHandler(config));
		this.name = name;
		if (name == null) {
			throw new NullPointerException("The name should not be null");
		}
		logger.trace("Creating TiesDB Service Daemon...");
		ThreadGroup tiesThreadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), name);
		tiesServiceThread = new Thread(tiesThreadGroup, this, "TiesServiceDaemon:" + name);
		tiesServiceThread.setDaemon(false);
		logger.trace("TiesDB Service Daemon created successfully");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void start() throws TiesException {
		logger.trace("Starting TiesDB Service Daemon...");
		try {
			tiesServiceThread.start();
		} catch (Throwable e) {
			throw new TiesException("Failed to start TiesDB Service", e);
		}
		logger.trace("TiesDB Service Daemon started successfully");
	}

	@Override
	public void stop() throws TiesException {
		logger.trace("Stopping TiesDB Service Daemon...");
		try {
			stopInternal();
			tiesServiceThread.join();
		} catch (Throwable e) {
			throw new TiesException("Failed to stop TiesDB Service Daemon", e);
		}
		logger.trace("TiesDB Service Daemon stopped successfully");
	}

	private void stopInternal() {
		// stopWebSocketServer();
	}

	// private void stopWebSocketServer() {
	// if (webSocketServer != null) {
	// webSocketServer.stop();
	// }
	// }
	//
	// private void startWebSocketServer() {
	// webSocketServer = new WebSocketServer();
	// try {
	// webSocketServer.start();
	// } catch (Exception e) {
	// logger.error("WebSocketServer {} failure", webSocketServer, e);
	// }
	// }

	@Override
	public void run() {
		try {
			logger.trace("Running TiesDB Service Daemon...");
			main();
			logger.trace("Finishing TiesDB Service Daemon...");
		} catch (Throwable e) {
			logger.error("TiesDB Service Daemon execution failed with exception", e);
			if (config.isServiceStopCritical()) {
				logger.debug("TiesDB Service is vital, execution will be stopped", e);
				System.exit(-1);
			}
		}
		logger.debug("TiesDB Service Daemon finished successfully");
	}

	@Override
	public void init() {
		logger.trace("Initializing TiesDB Service Daemon...");
		// TODO move to run() and do the unregister after main();
		registerMigrationListener();
		logger.trace("TiesDB Service Daemon initialized successfully");
	}

	private void registerMigrationListener() {
		logger.trace("Waiting for MigrationManager is ready for bootstrap...");
		MigrationManager.waitUntilReadyForBootstrap();
		logger.trace("MigrationManager is ready for bootstrap");
		logger.trace("Registering {}...", TiesMigrationListener.class.getSimpleName());
		MigrationManager.instance.register(new TiesMigrationListener());
		logger.debug("{} registered successfully", TiesMigrationListener.class.getSimpleName());
	}

	private void main() {
		// startWebSocketServer();
	}

	private final class TiesMigrationListener extends MigrationListener {
		@Override
		public void onCreateKeyspace(String ksName) {
			logger.debug("TiesDB keyspace created {}", ksName);
			super.onCreateKeyspace(ksName);
		}

		@Override
		public void onDropKeyspace(String ksName) {
			logger.debug("TiesDB keyspace removed {}", ksName);
			super.onDropKeyspace(ksName);
		}

	}

	@Override
	public TiesService getService() throws TiesException {
		return this;
	}
}
