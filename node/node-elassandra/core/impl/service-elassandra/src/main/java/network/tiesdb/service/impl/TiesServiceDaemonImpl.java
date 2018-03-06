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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.context.api.TiesServiceConfig;
import network.tiesdb.exception.TiesConfigurationException;
import network.tiesdb.exception.TiesException;
import network.tiesdb.service.api.TiesServiceDaemon;

/**
 * TiesDB basic service daemon implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesServiceDaemonImpl extends TiesServiceImpl implements Runnable, TiesServiceDaemon {

	private static final Logger logger = LoggerFactory.getLogger(TiesServiceDaemonImpl.class);

	private final Thread tiesServiceThread;

	public TiesServiceDaemonImpl(String name, TiesServiceConfig config) throws TiesConfigurationException {
		super(name, config);
		logger.trace("Creating TiesDB Service Daemon...");
		ThreadGroup tiesThreadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), name);
		tiesServiceThread = new Thread(tiesThreadGroup, this, "TiesServiceDaemon:" + name);
		tiesServiceThread.setDaemon(false);
		logger.trace("TiesDB Service Daemon created successfully");
	}

	@Override
	public void run() {
		logger.trace("Starting TiesDB Service Daemon...");
		try {
			super.initInternal();
			super.run();
		} catch (TiesException e) {
			logger.error("Failed to start TiesDB Service", e);
			System.exit(-2);
		}
		logger.trace("TiesDB Service Daemon started successfully");
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
			super.stopInternal();
			tiesServiceThread.join();
		} catch (Throwable e) {
			logger.debug("Failed to stop TiesDB Service Daemon", e);
			logger.trace("Interrupting TiesDB Service Daemon...");
			tiesServiceThread.interrupt();
			throw new TiesException("TiesDB Service Daemon stopped by interrupt", e);
		}
		logger.trace("TiesDB Service Daemon stopped successfully");
	}

	@Override
	public void init() throws TiesException {
		logger.trace("Initializing TiesDB Service Daemon...");
		// NOP: Internal TiesDB Service initialization occurs in a tiesServiceThread
		logger.trace("TiesDB Service Daemon initialized successfully");
	}

	@Override
	public TiesServiceImpl getService() throws TiesConfigurationException {
		return this;
	}

	@Override
	public TiesServiceDaemonImpl getDaemon() {
		return this;
	}

	@Override
	public String getName() {
		return name;
	}
}
