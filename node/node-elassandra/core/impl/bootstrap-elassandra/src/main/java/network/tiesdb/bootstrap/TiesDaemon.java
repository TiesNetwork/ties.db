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

import java.util.concurrent.atomic.AtomicReference;

import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.logging.logback.LogbackESLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.context.api.TiesContext;
import network.tiesdb.exception.TiesStartupException;

/**
* TiedDB Daemon Main Class.
* 
* <P>Entry point to run TiesDB with underlying services.
* 
* @author Anton Filatov (filatov@ties.network)
*/
public class TiesDaemon extends TiesBootstrap {

	private static final Logger logger = LoggerFactory.getLogger(TiesDaemon.class);

	static {
		try {
			ESLoggerFactory.setDefaultFactory(new LogbackESLoggerFactory());
		} catch (Exception e) {
			System.err.println("Failed to configure logging " + e.toString());
			e.printStackTrace(System.err);
		}
	}

	public static TiesDaemon instance = new TiesDaemon();

	public final AtomicReference<TiesContext> context = new AtomicReference<>();

	public static void main(String[] args) {
		try {
			instance.init(args);
		} catch (TiesStartupException e) {
			logger.error("Could not start TiesDB", e);
			System.exit(e.getExitCode());
		}
	}

}
