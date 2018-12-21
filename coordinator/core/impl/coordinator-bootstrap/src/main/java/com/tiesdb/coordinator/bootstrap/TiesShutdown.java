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
package com.tiesdb.coordinator.bootstrap;

import java.lang.ref.WeakReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.exception.TiesException;
import network.tiesdb.service.api.TiesServiceDaemon;

/**
 * TiesDB shutdown.
 * 
 * <P>
 * Main logic for TiesDB service shutdown.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesShutdown implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TiesShutdown.class);

    private final WeakReference<TiesServiceDaemon> daemonRef;

    public static void addShutdownHook(TiesServiceDaemon daemon) {
        logger.trace("Adding TiesDB Service Daemon shutdown hook..");
        if (null == daemon) {
            throw new NullPointerException("The daemon should not be null");
        }
        String name = daemon.getName();
        name = null == name ? "?" : name;
        logger.trace("TiesDB Service Daemon shutdown hook for daemon \"{}\"", name);
        ThreadGroup tiesThreadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), name);
        Thread shutdownHook = new Thread(tiesThreadGroup, new TiesShutdown(daemon), "TiesServiceShutdown:" + name);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        logger.trace("TiesDB Service Daemon shutdown hook added successfully");
    }

    private TiesShutdown(TiesServiceDaemon daemon) {
        this.daemonRef = new WeakReference<>(daemon);
    }

    @Override
    public void run() {
        logger.trace("Starting the TiesDB Service Daemon shutdown procedure");
        if (!daemonRef.isEnqueued()) {
            TiesServiceDaemon daemon = daemonRef.get();
            if (null != daemon) {
                logger.trace("Shutting down TiesDB Service Daemon...");
                try {
                    daemon.stop();
                    logger.trace("TiesDB Service Daemon shutted down successfully");
                } catch (TiesException e) {
                    logger.error("Failed to shutdown TiesDB Service Daemon", e);
                }
            } else {
                logger.trace("TiesDB Service Daemon reference is obsolete or collected by garbage collector");
            }
        } else {
            logger.trace("TiesDB Service Daemon is enqueued to garbage collection, no need to shutdown");
        }
    }

}
