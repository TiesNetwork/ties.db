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
package network.tiesdb.coordinator.service.schema;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.api.TiesDaemon;

public class TiesServiceSchemaDaemonImpl implements TiesDaemon {

    private static final Logger LOG = LoggerFactory.getLogger(TiesServiceSchemaDaemonImpl.class);

    private static volatile boolean isCleanFailed = false;
    private static boolean isClean = false;

    private final AtomicReference<ScheduledExecutorService> schedulerRef = new AtomicReference<ScheduledExecutorService>();
    private final AtomicReference<ScheduledFuture<?>> checkerRef = new AtomicReference<ScheduledFuture<?>>();
    private final AtomicReference<ScheduledFuture<?>> cleanerRef = new AtomicReference<ScheduledFuture<?>>();

    private final TiesServiceSchema schemaService;

    private static final long DEFAULT_CHECK_DELAY = 180;
    private static final TimeUnit DEFAULT_CHECK_DELAY_UNIT = TimeUnit.MINUTES;

    private static final long DEFAULT_CLEANUP_DELAY = DEFAULT_CHECK_DELAY * 3;
    private static final TimeUnit DEFAULT_CLEANUP_DELAY_UNIT = TimeUnit.MINUTES;

    private static final int EXECUTOR_CORE_POOL_SIZE = 2;

    public TiesServiceSchemaDaemonImpl(TiesServiceSchema schemaService) {
        this.schemaService = schemaService;
    }

    private ScheduledExecutorService getScheduler() {
        ScheduledExecutorService scheduler = schedulerRef.get();
        if (null == scheduler || scheduler.isShutdown()) {
            throw new IllegalStateException("TiesServiceSchema scheduler was not registered or is already stopped.");
        }
        return scheduler;
    }

    public void start() {
        LOG.debug("Starting TiesServiceSchema...");
        if (!isCleanFailed) {
            LOG.debug("Running TiesServiceSchema cleaner...");
            runCleanerOnce(getScheduler(), new AllSchemaCleaner());
        } else {
            LOG.debug("Failed to run TiesServiceSchema cleaner. Cleanup failed...");
        }
        checkerRef.updateAndGet((checker) -> {
            if (null == checker) {
                LOG.debug("Scheduling TiesServiceSchema checker...");
                checker = getScheduler().scheduleAtFixedRate(new AllSchemaChecker(), 0, DEFAULT_CHECK_DELAY, DEFAULT_CHECK_DELAY_UNIT);
                LOG.debug("TiesServiceSchema checker scheduled");
            }
            return checker;
        });
        cleanerRef.updateAndGet((cleaner) -> {
            if (null == cleaner) {
                LOG.debug("Scheduling TiesServiceSchema cleaner...");
                cleaner = getScheduler().scheduleAtFixedRate(new AllSchemaChecker(), 0, DEFAULT_CLEANUP_DELAY, DEFAULT_CLEANUP_DELAY_UNIT);
                LOG.debug("TiesServiceSchema cleaner scheduled");
            }
            return cleaner;
        });
        LOG.debug("TiesServiceSchema started");
    }

    private static synchronized void runCleanerOnce(ScheduledExecutorService scheduler, Runnable cleaner) {
        if (!isClean && !isCleanFailed) {
            try {
                scheduler.submit(cleaner).get();
                isClean = true;
            } catch (Throwable e) {
                LOG.warn("Schema cleaning failed", e);
                isCleanFailed = true;
            }
        }
    }

    public void stop() {
        LOG.trace("Stopping TiesServiceSchema...");

        ScheduledExecutorService scheduler = schedulerRef.getAndSet(null);
        if (null != scheduler) {
            LOG.trace("Stopping TiesServiceSchema scheduler...");
            scheduler.shutdown();
            LOG.trace("TiesServiceSchema scheduler stopped");
        }

        LOG.debug("TiesServiceSchema stopped");
    }

    public void init() {
        LOG.debug("Initializing TiesServiceSchema...");
        initScheduler();
        LOG.debug("TiesServiceSchema initialized");
    }

    private void initScheduler() {
        LOG.trace("Creating TiesServiceSchema scheduler");
        ScheduledExecutorService scheduler = //
                Executors.newScheduledThreadPool(EXECUTOR_CORE_POOL_SIZE, (r) -> new Thread(r, "TiesServiceSchemaUpdateScheduler"));
        LOG.trace("Registering created TiesServiceSchema scheduler");
        if (!schedulerRef.compareAndSet(null, scheduler)) {
            LOG.trace("TiesServiceSchema scheduler registration failed");
            LOG.trace("Stopping TiesServiceSchema scheduler...");
            scheduler.shutdownNow();
            LOG.trace("TiesServiceSchema scheduler stopped");
            throw new IllegalStateException("TiesServiceSchema scheduler is already registered");
        }
    }

    public class AllSchemaChecker implements Runnable {

        @Override
        public void run() {
            LOG.trace("Checking for scheduled schema updates...");
            schemaService.updateAllDescriptors();
            LOG.trace("Checking for schema update errors...");
            schemaService.retryUpdateFailedDescriptors();
            LOG.trace("All schema updates checked");
        }

    }

    public class AllSchemaCleaner implements Runnable {

        @Override
        public void run() {
            LOG.debug("Cleaning for any schema update garbage...");
            schemaService.garbadgeCleanup();
            LOG.debug("All schema update garbage removed");
        }

    }

}
