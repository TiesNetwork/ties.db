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

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.context.api.TiesContext;
import network.tiesdb.exception.TiesStartupException;

/**
 * Bootstrap class for TiesDB.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(TiesBootstrap.class);

    final AtomicReference<TiesContext> contextRef = new AtomicReference<>();

    public void init(String... args) throws TiesStartupException {
        logger.debug("Launching TiesDB services");
        initTiesDb();
        logger.debug("TiesDB services launched successfully");
    }

    // TODO add a parameter or make another function for synchronous
    // initialization
    private void initTiesDb() {
        ThreadGroup tiesThreadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), "TiesDB");
        Thread tiesInitThread = new Thread(tiesThreadGroup, new TiesInitialization(this), "TiesInitialization");
        tiesInitThread.setDaemon(false);
        tiesInitThread.start();
    }
}
