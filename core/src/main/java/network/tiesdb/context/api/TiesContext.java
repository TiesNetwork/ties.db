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
package network.tiesdb.context.api;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;

/**
 * Context of TiesDB service.
 * 
 * <P>Contains configuration of TiesDB service and service runtime state.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesContext {

	public static class TiesConfig {

		private String serviceAddress = InetAddress.getLoopbackAddress().getHostAddress();
		private boolean serviceStopCritical = true;
		private String serviceVersion;

		public String getServiceAddress() {
			return serviceAddress;
		}

		public void setServiceAddress(String serviceAddress) {
			this.serviceAddress = serviceAddress;
		}

		public boolean isServiceStopCritical() {
			return serviceStopCritical;
		}

		public void setServiceStopCritical(boolean serviceStopCritical) {
			this.serviceStopCritical = serviceStopCritical;
		}

		public String getServiceVersion() {
			return serviceVersion;
		}

		public void setServiceVersion(String serviceVersion) {
			this.serviceVersion = serviceVersion;
		}
	}

	private Map<String, TiesConfig> config = Collections.singletonMap("default", new TiesConfig());

	public Map<String, TiesConfig> getConfig() {
		return config;
	}

	public void setConfig(Map<String, TiesConfig> configs) {
		this.config = configs;
	}
}
