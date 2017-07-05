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

import java.util.Arrays;

import org.apache.cassandra.service.CassandraDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.exception.TiesStartupException;

/**
 * Bootstrap class for TiesDB.
 * 
 * <P>Contains boot and initialization sequences for TiesDB bootstrapping.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesBootstrap {

	private static final Logger logger = LoggerFactory.getLogger(TiesBootstrap.class);

	private static final Class<?> DEFAULT_DELEGATE_CLASS = CassandraDaemon.class;

	public void init(String... args) throws TiesStartupException {
		if (args.length > 0 && args[0].equals("--no-storage")) {
			logger.debug("Found TiesDB --no-storage option. No storage capability will be exposed");
			initTiesDb();
		} else {
			logger.trace("Searching storage system class");
			Class<?> delegateClass = DEFAULT_DELEGATE_CLASS;
			String[] delegateArgs = args;
			if (args.length > 0) {
				try {
					delegateClass = Thread.currentThread().getContextClassLoader().loadClass(args[0]);
				} catch (ClassNotFoundException e) {
					throw new TiesStartupException(1, "Could not find delegate startup class \"" + args[0] + "\"", e);
				}
				delegateArgs = Arrays.copyOfRange(args, 1, args.length);
			}
			logger.debug("Storage system class found {}", delegateClass.getName());
			initTiesDb();
			logger.debug("Switching to the storage system boot process");
			try {
				delegateClass.getDeclaredMethod("main", String[].class).invoke(delegateClass, (Object) delegateArgs);
			} catch (Throwable e) {
				throw new TiesStartupException(2,
						"Could not start storage system boot process from class \"" + delegateClass.getName() + "\"",
						e);
			}
		}
	}

	private void initTiesDb() throws TiesStartupException {
		logger.trace("Starting TiesDB boot sequence...");
		TiesInitDaemon initDaemon = new TiesInitDaemon();
		ThreadGroup tiesThreadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), "TiesDB");
		Thread tiesInitThread = new Thread(tiesThreadGroup, initDaemon, "TiesInitDaemon");
		tiesInitThread.setDaemon(true);
		tiesInitThread.start();
		try {
			tiesInitThread.join();
		} catch (InterruptedException e) {
			throw new TiesStartupException(2, "TiesDB boot sequence failed", e);
		}
		if (initDaemon.getInitializationException() != null) {
			throw new TiesStartupException(2, "TiesDB initialization failed", initDaemon.getInitializationException());
		}
		logger.trace("Boot sequence of TiesDB finished successfully");
	}
}
