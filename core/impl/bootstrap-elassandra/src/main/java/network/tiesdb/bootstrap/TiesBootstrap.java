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

	private static final String DEFAULT_DELEGATE_CLASS_NAME = "org.apache.cassandra.service.ElassandraDaemon";

	public void init(String... args) throws TiesStartupException {
		if (args.length > 0 && args[0].equals("--no-storage")) {
			logger.debug("Found TiesDB --no-storage option. No storage capability will be exposed");
			initTiesDb();
		} else {
			logger.trace("Searching storage system class");
			String[] delegateArgs = args;
			String delegateClassName = DEFAULT_DELEGATE_CLASS_NAME;
			if (args.length > 0) {
				delegateClassName = args[0];
				delegateArgs = Arrays.copyOfRange(args, 1, args.length);
			}
			Class<?> delegateClass;
			try {
				delegateClass = Thread.currentThread().getContextClassLoader().loadClass(delegateClassName);
			} catch (ClassNotFoundException e) {
				throw new TiesStartupException(1, "Could not find delegate startup class \"" + delegateClassName + "\"", e);
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

	// TODO add a parameter or make another function for synchronous
	// initialization
	private void initTiesDb() {
		ThreadGroup tiesThreadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), "TiesDB");
		Thread tiesInitThread = new Thread(tiesThreadGroup, new TiesInitialization(), "TiesInitialization");
		tiesInitThread.setDaemon(false);
		tiesInitThread.start();
	}
}
