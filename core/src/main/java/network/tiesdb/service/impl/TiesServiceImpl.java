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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.api.TiesVersion;
import network.tiesdb.api.TiesVersions;
import network.tiesdb.context.api.TiesServiceConfig;
import network.tiesdb.context.api.TiesTransportConfig;
import network.tiesdb.exception.TiesConfigurationException;
import network.tiesdb.exception.TiesException;
import network.tiesdb.handler.api.TiesHandler;
import network.tiesdb.service.api.TiesService;
import network.tiesdb.transport.api.TiesTransport;
import network.tiesdb.transport.api.TiesTransportDaemon;

/**
 * TiesDB service implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public abstract class TiesServiceImpl implements TiesService, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(TiesServiceImpl.class);

	private static final TiesServiceImplVersion IMPLEMENTATION_VERSION = TiesServiceImplVersion.v_0_0_1_prealpha;

	protected final TiesServiceConfig config;

	protected final String name;

	private final AtomicReference<List<TiesTransport>> transportsRef = new AtomicReference<>();
	private final TiesMigrationListenerImpl migrationListener;
	private final AtomicReference<TiesHandler> handlerRef = new AtomicReference<>();

	private final TiesVersions versions = new TiesVersions() {

		@Override
		public TiesVersion tiesVersion() {
			return IMPLEMENTATION_VERSION.getApiVersion();
		}

		@Override
		public TiesVersion serviceVersion() {
			return IMPLEMENTATION_VERSION;
		}

		@Override
		public TiesVersion handlerVersion() {
			TiesHandler handler = handlerRef.get();
			return null == handler ? null : handler.getVersion();
		}

		@Override
		public List<TiesVersion> transportVersion() {
			List<TiesTransport> transports = transportsRef.get();
			if (null == transports || transports.isEmpty()) {
				return Collections.emptyList();
			} else {
				List<TiesVersion> result = new ArrayList<>(transports.size());
				for (Iterator<TiesTransport> iterator = transports.iterator(); iterator.hasNext();) {
					result.add(iterator.next().getVersion());
				}
				return result;
			}
		}

	};

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
			if (null == config.isServiceStopCritical() || config.isServiceStopCritical()) {
				logger.debug("TiesDB Service is vital, execution will be stopped", e);
				System.exit(-1);
			}
		}
	}

	protected void initInternal() throws TiesException {
		initTransportDaemons();
	}

	protected void initTransportDaemons() throws TiesConfigurationException {
		logger.trace("Creating TiesDB Service Transport Daemons...");
		List<TiesTransportConfig> transportsConfigs = config.getTransportConfigs();
		List<TiesTransport> transports = new ArrayList<>(transportsConfigs.size());
		for (TiesTransportConfig tiesTransportConfig : transportsConfigs) {
			try {
				TiesTransportDaemon transportDaemon = tiesTransportConfig.getTiesTransportFactory()
						.createTransportDaemon(this, tiesTransportConfig);
				transports.add(transportDaemon.getTiesTransport());
			} catch (TiesConfigurationException e) {
				logger.error("Failed to create TiesDB Transport Daemon", e);
			}
		}
		if (!transportsRef.compareAndSet(null, Collections.unmodifiableList(transports))) {
			throw new TiesConfigurationException("TiesDB Transports were initialized already");
		}
	}

	protected void stopInternal() {
		logger.trace("Stopping TiesDB Service Transports...");
		List<TiesTransport> transports = transportsRef.get();
		if (null == transports) {
			logger.trace("No TiesDB Service Transports to stop");
		} else {
			for (TiesTransport tiesTransport : transports) {
				try {
					nullsafe(tiesTransport.getDaemon()).stop();
				} catch (Throwable e) {
					logger.error("Failed to stop TiesDB Transport", e);
				}
			}
		}
		migrationListener.unregisterMigrationListener();
	}

	private void runInternal() throws TiesException {
		migrationListener.registerMigrationListener();
		logger.trace("Starting TiesDB Service Transports...");
		List<TiesTransport> transports = transportsRef.get();
		if (null == transports) {
			logger.trace("No TiesDB Service Transports to start");
		} else {
			for (TiesTransport tiesTransport : transports) {
				try {
					TiesTransportDaemon daemon = nullsafe(tiesTransport.getDaemon());
					daemon.init();
					daemon.start();
				} catch (Throwable e) {
					logger.error("Failed to init and start TiesDB Transport", e);
				}
			}
		}
	}

	@Override
	public TiesVersion getVersion() {
		return versions.serviceVersion();
	}

	@Override
	public TiesVersions getVersions() {
		return versions;
	}

}