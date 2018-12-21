/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package network.tiesdb.coordinator.service.impl;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.api.TiesVersion;
import network.tiesdb.context.api.TiesRouterConfig;
import network.tiesdb.context.api.TiesSchemaConfig;
import network.tiesdb.context.api.TiesTransportConfig;
import network.tiesdb.coordinator.service.impl.scope.TiesCoordinatedRequestPool;
import network.tiesdb.coordinator.service.impl.scope.TiesCoordinatorServiceScopeImpl;
import network.tiesdb.coordinator.service.schema.TiesServiceSchema;
import network.tiesdb.coordinator.service.schema.TiesServiceSchemaDaemonImpl;
import network.tiesdb.exception.TiesConfigurationException;
import network.tiesdb.exception.TiesException;
import network.tiesdb.router.api.TiesRouter;
import network.tiesdb.router.api.TiesRouterFactory;
import network.tiesdb.schema.api.TiesSchemaFactory;
import network.tiesdb.service.api.TiesService;
import network.tiesdb.service.scope.api.TiesServiceScope;
import network.tiesdb.service.scope.api.TiesServiceScopeException;
import network.tiesdb.service.scope.api.TiesServiceScopeResult;
import network.tiesdb.transport.api.TiesTransportServer;

public class TiesCoordinatorServiceImpl implements TiesService {

    private static final Logger LOG = LoggerFactory.getLogger(TiesCoordinatorServiceImpl.class);

    private static final TiesCoordinatorServiceVersionImpl IMPLEMENTATION_VERSION = TiesCoordinatorServiceVersionImpl.v_0_0_1_prealpha;

    private final AtomicReference<List<TiesTransportServer>> transportsRef = new AtomicReference<>();
    private final AtomicReference<TiesServiceSchemaDaemonImpl> schemaServiceDaemonRef = new AtomicReference<>();
    private final AtomicReference<TiesServiceSchema> schemaServiceRef = new AtomicReference<>();
    private final AtomicReference<TiesRouter> routerServiceRef = new AtomicReference<>();

    private final TiesCoordinatorServiceConfigImpl config;
    private final TiesCoordinatedRequestPool<TiesServiceScopeResult.Result> requestPool;

    public TiesCoordinatorServiceImpl(TiesCoordinatorServiceConfigImpl config) throws TiesConfigurationException {
        if (null == config) {
            throw new NullPointerException("The config should not be null");
        }
        this.config = config;
        this.requestPool = new TiesCoordinatedRequestPool<>(8);
    }

    @Override
    public TiesVersion getVersion() {
        return IMPLEMENTATION_VERSION;
    }

    @Override
    public TiesServiceScope newServiceScope() {
        return new TiesCoordinatorServiceScopeImpl(this);
    }

    public TiesServiceSchema getSchemaService() throws TiesServiceScopeException {
        TiesServiceSchema schemaImpl = schemaServiceRef.get();
        if (null == schemaImpl) {
            throw new TiesServiceScopeException("TiesDB Schema Service have not been initialized");
        }
        return schemaImpl;
    }

    public TiesRouter getRouterService() throws TiesServiceScopeException {
        TiesRouter router = routerServiceRef.get();
        if (null == router) {
            throw new TiesServiceScopeException("TiesDB Router Service have not been initialized");
        }
        return router;
    }

    public void init() throws TiesException {
        LOG.trace("Initializing TiesDB Coordinator...");
        initSchemaService();
        initTiesTransports();
        initRouterService();
        LOG.trace("TiesDB Coordinator initialized");
    }

    public void start() throws TiesException {
        LOG.trace("Starting TiesDB Coordinator...");
        startSchemaService();
        startTiesTransports();
        LOG.trace("TiesDB Coordinator started");
    }

    public void stop() {
        LOG.trace("Stopping TiesDB Coordinator...");
        stopSchemaService();
        stopTiesTransports();
        LOG.trace("TiesDB Coordinator stopped");
    }

    private void initRouterService() throws TiesConfigurationException {
        LOG.trace("Creating TiesDB Router Service...");
        TiesRouterConfig routerConfig = config.getRouterConfig();
        if (null == routerConfig) {
            throw new TiesConfigurationException("No TiesDB Router configuration found");
        }
        TiesRouterFactory routerFactory = routerConfig.getTiesRouterFactory();
        requireNonNull(routerFactory, "TiesDB Router Factory not found");

        try {
            routerServiceRef.updateAndGet(v -> {
                if (null != v) {
                    LOG.warn("TiesDB Schema Router Service have already been inialized");
                    return v;
                }
                return routerFactory.createRouter(this);
            }).init();
        } catch (TiesException e) {
            throw new TiesConfigurationException("Failed to initialize TiesDB Schema Router Service", e);
        }
    }

    private void initSchemaService() throws TiesConfigurationException {
        LOG.trace("Creating TiesDB Schema Connection...");
        TiesSchemaConfig schemaConfig = config.getSchemaConfig();
        if (null == schemaConfig) {
            throw new TiesConfigurationException("No TiesDB Schema configuration found");
        }
        TiesSchemaFactory schemaFactory = schemaConfig.getTiesSchemaFactory();
        requireNonNull(schemaFactory, "TiesDB Schema Factory not found");

        TiesServiceSchema schemaService = schemaServiceRef.updateAndGet(v -> {
            if (null != v) {
                LOG.warn("TiesDB Schema Service have already been inialized");
                return v;
            }
            return new TiesServiceSchema(schemaFactory.createSchema(this));
        });
        schemaServiceDaemonRef.compareAndSet(null, new TiesServiceSchemaDaemonImpl(schemaService));
    }

    private void startSchemaService() throws TiesConfigurationException {
        LOG.trace("Starting TiesDB Schema Service Daemon...");
        TiesServiceSchemaDaemonImpl serviceSchemaDaemon = schemaServiceDaemonRef.get();
        if (null == serviceSchemaDaemon) {
            throw new TiesConfigurationException("No TiesDB Schema Service Daemon to start");
        } else {
            try {
                serviceSchemaDaemon.init();
                serviceSchemaDaemon.start();
            } catch (Throwable e) {
                LOG.error("Failed to init and start TiesDB Schema Service Daemon", e);
            }
        }
    }

    private void stopSchemaService() {
        LOG.trace("Stopping TiesDB Schema Service Daemon...");
        TiesServiceSchemaDaemonImpl serviceSchemaDaemon = schemaServiceDaemonRef.get();
        if (null == serviceSchemaDaemon) {
            LOG.trace("No TiesDB Schema Service Daemon to stop");
        } else {
            try {
                serviceSchemaDaemon.stop();
            } catch (Throwable e) {
                LOG.error("Failed to stop TiesDB Schema Service Daemon", e);
            }
        }
    }

    private void initTiesTransports() throws TiesConfigurationException {
        LOG.trace("Creating TiesDB Coordinator Transport Daemons...");
        List<TiesTransportConfig> transportsConfigs = config.getTransportConfigs();
        List<TiesTransportServer> transports = new ArrayList<>(transportsConfigs.size());
        for (TiesTransportConfig tiesTransportConfig : transportsConfigs) {
            try {
                TiesTransportServer transportDaemon = tiesTransportConfig.getTiesTransportFactory().createTransportServer(this);
                transports.add(transportDaemon);
            } catch (TiesConfigurationException e) {
                LOG.error("Failed to create TiesDB Coordinator Transport Daemon", e);
            }
        }
        if (!transportsRef.compareAndSet(null, Collections.unmodifiableList(transports))) {
            throw new TiesConfigurationException("TiesDB Coordinator Transports have already been initialized");
        }
    }

    private void startTiesTransports() throws TiesConfigurationException {
        LOG.trace("Starting TiesDB Coordinator Transports...");
        List<TiesTransportServer> transports = transportsRef.get();
        if (null == transports) {
            throw new TiesConfigurationException("No TiesDB Coordinator Transports to start");
        } else {
            int count = 0;
            for (TiesTransportServer daemon : transports) {
                try {
                    daemon.init();
                    daemon.start();
                    count++;
                } catch (Throwable e) {
                    LOG.error("Failed to init and start TiesDB Coordinator Transport", e);
                }
            }
            if (0 >= count) {
                throw new TiesConfigurationException("Not a single TiesDB Coordinator Transports has been successfully started");
            }
        }
    }

    private void stopTiesTransports() {
        LOG.trace("Stopping TiesDB Coordinator Transports...");
        List<TiesTransportServer> transportDaemons = transportsRef.get();
        if (null == transportDaemons) {
            LOG.trace("No TiesDB Coordinator Transports to stop");
        } else {
            for (TiesTransportServer daemon : transportDaemons) {
                try {
                    daemon.stop();
                } catch (Throwable e) {
                    LOG.error("Failed to stop TiesDB Coordinator Transport", e);
                }
            }
        }
    }

    public TiesCoordinatedRequestPool<TiesServiceScopeResult.Result> getRequestPool() {
        return requestPool;
    }

}
