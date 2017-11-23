package com.tiesdb.protocol.api;

import java.util.Comparator;

import com.tiesdb.protocol.api.data.Version;
import com.tiesdb.protocol.api.data.Version.VersionComparator;
import com.tiesdb.protocol.api.data.Version.VersionCompratorThreshold;
import com.tiesdb.protocol.exception.TiesDBProtocolException;

public interface TiesDBProtocol {

	public static final Comparator<TiesDBProtocol> PROTOCOL_COMPARATOR = new Comparator<TiesDBProtocol>() {
		private final Comparator<Version> verComp = new VersionComparator(VersionCompratorThreshold.FULL);
		@Override
		public int compare(TiesDBProtocol p1, TiesDBProtocol p2) {
			return verComp.compare(p1 == null ? null : p1.getVersion(), p2 == null ? null : p2.getVersion());
		}
	};

	Version getVersion();

	void acceptPacket(TiesDBProtocolPacketChannel packetChannel, TiesDBProtocolHandler handler)
			throws TiesDBProtocolException;

}
