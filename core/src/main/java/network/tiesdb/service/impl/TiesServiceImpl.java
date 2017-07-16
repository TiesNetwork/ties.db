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

import static network.tiesdb.util.Safecheck.nullsafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.api.TiesHandler;
import network.tiesdb.api.TiesService;
import network.tiesdb.api.TiesVersion;
import network.tiesdb.context.api.TiesServiceConfig;
import network.tiesdb.context.api.TiesTransportConfig;
import network.tiesdb.exception.TiesConfigurationException;
import network.tiesdb.exception.TiesException;
import network.tiesdb.exception.util.MessageHelper;
import network.tiesdb.service.impl.handler.TiesHandlerConfigImpl;
import network.tiesdb.transport.api.TiesTransportDaemon;

/**
 * TiesDB service implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public abstract class TiesServiceImpl implements TiesService, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(TiesServiceImpl.class);

	private static final TiesImplVersion IMPLEMENTATION_VERSION = TiesImplVersion.v_0_0_1_prealpha;

	protected final TiesServiceConfig config;

	protected final String name;

	private final AtomicReference<List<TiesTransportDaemon>> transportDaemonsRef = new AtomicReference<>();
	private final TiesMigrationListenerImpl migrationListener;
	private final AtomicReference<TiesHandler> handlerRef = new AtomicReference<>();

	public TiesServiceImpl(String name, TiesServiceConfig config) {
		if (null == config) {
			throw new NullPointerException("The config should not be null");
		}
		this.config = config;
		this.name = name;
		this.migrationListener = createTiesMigrationListener();
	}

	protected TiesMigrationListenerImpl createTiesMigrationListener() {
		return new TiesMigrationListenerImpl(this);
	}

	@Override
	public TiesServiceConfig getTiesServiceConfig() {
		return config;
	}

	public void run() {
		try {
			logger.trace("Running TiesDB Service...");
			runInternal();
			logger.trace("TiesDB Service is ready and waiting for connections");
		} catch (Throwable e) {
			logger.error("TiesDB Service failed with exception", e);
			if (config.isServiceStopCritical()) {
				logger.debug("TiesDB Service is vital, execution will be stopped", e);
				System.exit(-1);
			}
		}
	}

	protected void initInternal() throws TiesException {
		initHandler();
		initTransportDaemons();
		migrationListener.registerMigrationListener();
	}

	protected void initHandler() throws TiesConfigurationException {
		TiesHandler handler = null;
		if (config instanceof TiesServiceConfigImpl) {
			handler = nullsafe(nullsafe(((TiesServiceConfigImpl) config).getHandler()).getTiesHandlerFactory())
					.createHandler(this);
		} else {
			logger.warn(MessageHelper.notFullyCompatible(config.getClass(), TiesServiceConfigImpl.class),
					"Using default settings for missing elements");
			TiesHandlerConfigImpl handlerConfig = new TiesHandlerConfigImpl();
			handler = handlerConfig.getTiesHandlerFactory().createHandler(this);
		}
		if (!handlerRef.compareAndSet(null, handler)) {
			throw new TiesConfigurationException("Handler have been already initialized");
		}
	}

	protected void initTransportDaemons() throws TiesConfigurationException {
		logger.trace("Creating TiesDB Service Transport Daemons...");
		List<TiesTransportConfig> transports = config.getTransports();
		List<TiesTransportDaemon> daemons = new ArrayList<>(transports.size());
		for (TiesTransportConfig tiesTransportConfig : transports) {
			try {
				TiesTransportDaemon transportDaemon = tiesTransportConfig.getTiesTransportFactory()
						.createTransportDaemon(this, tiesTransportConfig);
				daemons.add(transportDaemon);
			} catch (TiesConfigurationException e) {
				logger.error("Failed to create TiesDB Transport Daemon", e);
			}
		}
		if (!transportDaemonsRef.compareAndSet(null, Collections.unmodifiableList(daemons))) {
			throw new TiesConfigurationException("TiesDB Transport Daemons were initialized already");
		}
	}

	protected void stopInternal() {
		logger.trace("Stopping TiesDB Service Transport Daemons...");
		List<TiesTransportDaemon> daemons = transportDaemonsRef.get();
		if (null == daemons) {
			logger.trace("No TiesDB Service Transport Daemons to stop");
		} else {
			for (TiesTransportDaemon tiesTransportDaemon : daemons) {
				try {
					tiesTransportDaemon.stop();
				} catch (TiesException e) {
					logger.error("Failed to stop TiesDB Transport Daemon", e);
				}
			}
		}
		migrationListener.unregisterMigrationListener();
	}

	private void runInternal() throws TiesException {
		logger.trace("Starting TiesDB Service Transport Daemons...");
		List<TiesTransportDaemon> daemons = transportDaemonsRef.get();
		if (null == daemons) {
			logger.trace("No TiesDB Service Transport Daemons to start");
		} else {
			for (TiesTransportDaemon tiesTransportDaemon : daemons) {
				try {
					tiesTransportDaemon.init();
					tiesTransportDaemon.start();
				} catch (TiesException e) {
					logger.error("Failed to init and start TiesDB Transport Daemon", e);
				}
			}
		}
	}

	@Override
	public TiesHandler getHandler() {
		return handlerRef.get();
	}

	@Override
	public TiesVersion getApiVersion() {
		return IMPLEMENTATION_VERSION.getApiVersion();
	}

	@Override
	public TiesVersion getImplVersion() {
		return IMPLEMENTATION_VERSION;
	}

}