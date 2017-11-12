package com.tiesdb.protocol;

import java.util.Comparator;

public interface TiesDBProtocol {

	public static final Comparator<TiesDBProtocol> PROTOCOL_COMPARATOR = new Comparator<TiesDBProtocol>() {
		@Override
		public int compare(TiesDBProtocol p1, TiesDBProtocol p2) {
			return Version.VERSION_COMPARATOR.compare(p1 == null ? null : p1.getVersion(),
					p2 == null ? null : p2.getVersion());
		}
	};

	Version getVersion();

}
