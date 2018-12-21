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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.exception.TiesConfigurationException;
import network.tiesdb.exception.TiesException;
import network.tiesdb.service.api.TiesService;
import network.tiesdb.service.api.TiesServiceDaemon;

public class TiesCoordinatorServiceDaemonImpl implements Runnable, TiesServiceDaemon {

    private static final Logger logger = LoggerFactory.getLogger(TiesCoordinatorServiceDaemonImpl.class);

    private final Thread thread;
    private final TiesCoordinatorServiceImpl service;

    private final String name;

    public TiesCoordinatorServiceDaemonImpl(String name, TiesCoordinatorServiceImpl service) throws TiesConfigurationException {
        logger.trace("Creating TiesDB Coordinator Service \"{}\" Daemon...", name);
        ThreadGroup tiesThreadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), name);
        Thread thread = new Thread(tiesThreadGroup, this, "TiesServiceDaemon:" + name);
        thread.setDaemon(false);
        this.name = name;
        this.service = service;
        this.thread = thread;
        logger.trace("TiesDB Coordinator Service \"{}\" Daemon created successfully", this.name);
    }

    @Override
    public void run() {
        logger.trace("Starting TiesDB Coordinator Service \"{}\" Daemon...", this.name);
        try {
            service.init();
        } catch (TiesException e) {
            logger.error("Failed to initialize TiesDB Coordinator Service \"{}\"", this.name, e);
            System.exit(-2);
        }
        try {
            service.start();
        } catch (TiesException e) {
            logger.error("Failed to start TiesDB Coordinator Service\"{}\"", this.name, e);
            System.exit(-3);
        }
        logger.trace("TiesDB Coordinator Service \"{}\" Daemon started successfully", this.name);
    }

    @Override
    public void start() throws TiesException {
        logger.trace("Starting TiesDB Coordinator Service \"{}\" Daemon...", this.name);
        try {
            thread.start();
        } catch (Throwable e) {
            throw new TiesException("Failed to start TiesDB Coordinator Service", e);
        }
        logger.trace("TiesDB Coordinator Service \"{}\" Daemon started successfully", this.name);
    }

    @Override
    public void stop() throws TiesException {
        logger.trace("Stopping TiesDB Coordinator Service \"{}\" Daemon...", this.name);
        try {
            service.stop();
            // thread.join(); // Shutdown hangs on this if configuration failed
        } catch (Throwable e) {
            logger.debug("Failed to stop TiesDB Service Daemon", e);
            logger.trace("Interrupting TiesDB Service Daemon...");
            thread.interrupt();
            throw new TiesException("TiesDB Service Daemon stopped by interrupt", e);
        }
        logger.trace("TiesDB Coordinator Service \"{}\" Daemon stopped successfully", this.name);
    }

    @Override
    public void init() throws TiesException {
        // Daemon only initialization. Service initialization occurs in
        // tiesServiceThread on daemon start
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TiesService getService() {
        return service;
    }

    @Override
    public String toString() {
        return "TiesCoordinatorServiceDaemonImpl [name=" + name + ", service=" + service + ", thread=" + thread + "]";
    }

}
