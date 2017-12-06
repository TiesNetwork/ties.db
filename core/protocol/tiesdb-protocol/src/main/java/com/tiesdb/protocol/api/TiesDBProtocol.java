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
