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

import network.tiesdb.api.TiesApiVersion;
import network.tiesdb.api.TiesVersion;

/**
 * TiesDB implementation version implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public enum TiesServiceImplVersion implements TiesVersion {
	v_0_0_1_prealpha(TiesApiVersion.v_0_1_0_alpha, 0, 0, 1, "prealpha");

	private final Integer majorVersion;
	private final Integer minorVersion;
	private final Integer incrementalVersion;
	private final String qualifer;
	private final TiesApiVersion apiVersion;

	private TiesServiceImplVersion(TiesApiVersion apiVersion, Integer majorVersion, Integer minorVersion,
			Integer incrementalVersion) {
		this(apiVersion, majorVersion, minorVersion, incrementalVersion, null);
	}

	private TiesServiceImplVersion(TiesApiVersion apiVersion, Integer majorVersion, Integer minorVersion,
			Integer incrementalVersion, String qualifer) {
		if (null == apiVersion) {
			throw new NullPointerException("The apiVersion should not be null");
		}
		if (null == majorVersion) {
			throw new NullPointerException("The majorVersion should not be null");
		}
		if (null == minorVersion) {
			throw new NullPointerException("The minorVersion should not be null");
		}
		if (null == incrementalVersion) {
			throw new NullPointerException("The incrementalVersion should not be null");
		}
		if (null != qualifer && qualifer.isEmpty()) {
			qualifer = null;
		}
		this.apiVersion = apiVersion;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.incrementalVersion = incrementalVersion;
		this.qualifer = qualifer;
	}

	public TiesApiVersion getApiVersion() {
		return apiVersion;
	}

	@Override
	public Integer getMajorVersion() {
		return majorVersion;
	}

	@Override
	public Integer getMinorVersion() {
		return minorVersion;
	}

	@Override
	public Integer getIncrementalVersion() {
		return incrementalVersion;
	}

	@Override
	public String getQualifer() {
		return qualifer;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [" + ToString.format(this) + "]";
	}
}