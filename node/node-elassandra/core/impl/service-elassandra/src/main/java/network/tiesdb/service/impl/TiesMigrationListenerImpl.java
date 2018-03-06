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

/**
 * Apache Cassandra migration listener for TiesDB.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesMigrationListenerImpl extends MigrationListener {

	private static final Logger logger = LoggerFactory.getLogger(TiesMigrationListenerImpl.class);

	private final TiesServiceImpl service;

	public TiesMigrationListenerImpl(TiesServiceImpl service) {
		super();
		this.service = service;
	}

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

	void registerMigrationListener() {
		logger.trace("Waiting for MigrationManager is ready...");
		MigrationManager.waitUntilReadyForBootstrap();
		logger.trace("MigrationManager is ready");
		logger.trace("Registering {}...", TiesMigrationListenerImpl.class.getSimpleName());
		MigrationManager.instance.register(this);
		logger.debug("{} registered successfully for {}", this, service);
	}

	void unregisterMigrationListener() {
		logger.trace("Waiting for MigrationManager is ready...");
		MigrationManager.waitUntilReadyForBootstrap();
		logger.trace("MigrationManager is ready");
		logger.trace("Unregistering {}...", TiesMigrationListenerImpl.class.getSimpleName());
		MigrationManager.instance.unregister(this);
		logger.debug("{} unregistered successfully for {}", this, service);
	}
}