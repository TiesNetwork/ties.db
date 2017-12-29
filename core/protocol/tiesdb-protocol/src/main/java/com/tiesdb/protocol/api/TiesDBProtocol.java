/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project.If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.protocol.api;

import java.util.Comparator;

import com.tiesdb.protocol.api.data.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;

import static com.tiesdb.protocol.api.data.Version.VersionComprator;

public interface TiesDBProtocol {

	public static enum ProtocolComparator implements Comparator<TiesDBProtocol> {

		FULL(VersionComprator.FULL), //
		MAJOR(VersionComprator.MAJOR), //
		MINOR(VersionComprator.MINOR),//
		;

		private final VersionComprator versionComparatorThreshold;

		private ProtocolComparator(VersionComprator versionComparatorThreshold) {
			this.versionComparatorThreshold = versionComparatorThreshold;
		}

		@Override
		public int compare(TiesDBProtocol p1, TiesDBProtocol p2) {
			return p1 == null
				? (p2 == null ? 0 : 1)
				: p2 == null
					? -1 //
					: versionComparatorThreshold.compare(p1.getVersion(), p2.getVersion());
		}

	}

	Version getVersion();

	boolean createChannel(TiesDBProtocolPacketChannel packetChannel, TiesDBProtocolHandler handler) throws TiesDBProtocolException;

	boolean acceptChannel(TiesDBProtocolPacketChannel packetChannel, TiesDBProtocolHandler handler) throws TiesDBProtocolException;

}
