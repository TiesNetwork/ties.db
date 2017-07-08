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

import java.net.InetAddress;

import network.tiesdb.context.api.TiesTransportConfig;
import network.tiesdb.context.api.annotation.TiesConfigElement;
import network.tiesdb.transport.api.TiesTransportFactory;
import network.tiesdb.transport.impl.ws.TiesTransportFactoryImpl;

/**
 * TiesDB transport configuration implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
@TiesConfigElement(TiesTransportConfigImpl.BINDING)
public class TiesTransportConfigImpl implements TiesTransportConfig {

	public static final String BINDING = "network.tiesdb.transport.WebSocket";

	private String serviceAddress = InetAddress.getLoopbackAddress().getHostAddress();
	private Integer servicePort = -1;

	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	public Integer getServicePort() {
		return servicePort;
	}

	public void setServicePort(Integer servicePort) {
		this.servicePort = servicePort;
	}

	@Override
	public TiesTransportFactory getTiesTransportFactory() {
		return new TiesTransportFactoryImpl();
	}
}
