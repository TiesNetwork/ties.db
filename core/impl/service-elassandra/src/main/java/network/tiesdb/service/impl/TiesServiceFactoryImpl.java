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

import network.tiesdb.context.api.TiesServiceConfig;
import network.tiesdb.exception.TiesConfigurationException;
import network.tiesdb.service.api.TiesServiceFactory;

/**
 * TiesDB basic service factory implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesServiceFactoryImpl implements TiesServiceFactory {

	@Override
	public TiesServiceDaemonImpl createServiceDaemon(String name, TiesServiceConfig config) throws TiesConfigurationException {
		return new TiesServiceDaemonImpl(name, config);
	}

}
