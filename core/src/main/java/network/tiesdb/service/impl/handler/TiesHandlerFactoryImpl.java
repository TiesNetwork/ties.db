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
package network.tiesdb.service.impl.handler;

import network.tiesdb.api.TiesHandler;
import network.tiesdb.api.TiesService;
import network.tiesdb.handler.api.TiesHandlerFactory;
import network.tiesdb.service.impl.TiesServiceImpl;

/**
 * TiesDB handler factory implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesHandlerFactoryImpl implements TiesHandlerFactory {

	private TiesHandlerConfigImpl config;

	public TiesHandlerFactoryImpl(TiesHandlerConfigImpl config) {
		this.config = config;
	}

	@Override
	public TiesHandler createHandler(TiesService service) {
		if (null == service) {
			throw new NullPointerException("The service should not be null");
		}
		return new TiesHandlerImpl((TiesServiceImpl) service, config);
	}
}
