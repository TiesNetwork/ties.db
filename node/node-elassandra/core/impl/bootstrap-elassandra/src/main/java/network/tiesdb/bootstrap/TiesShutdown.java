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
package network.tiesdb.bootstrap;

import java.lang.ref.WeakReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.exception.TiesException;
import network.tiesdb.service.api.TiesServiceDaemon;

/**
 * TiesDB shutdown.
 * 
 * <P>Main logic for TiesDB service shutdown.
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
