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
package network.tiesdb.transport.impl.ws;

import network.tiesdb.context.api.TiesTransportConfig;
import network.tiesdb.exception.TiesConfigurationException;
import network.tiesdb.exception.TiesException;
import network.tiesdb.service.api.TiesService;
import network.tiesdb.transport.api.TiesTransport;
import network.tiesdb.transport.api.TiesTransportDaemon;

/**
 * TiesDB WebSock transport daemon implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesTransportDaemonImpl extends TiesTransportImpl implements TiesTransportDaemon {

	public TiesTransportDaemonImpl(TiesService service, TiesTransportConfig config) throws TiesConfigurationException {
		super(service, config);
	}

	@Override
	public TiesTransport getTiesTransport() throws TiesConfigurationException {
		return this;
	}

	@Override
	public void init() throws TiesException {
		super.initInternal();
	}

	@Override
	public void start() throws TiesException {
		super.startInternal();
	}

	@Override
	public void stop() throws TiesException {
		super.stopInternal();
	}

	@Override
	public TiesTransportDaemon getDaemon() {
		return this;
	}

}
