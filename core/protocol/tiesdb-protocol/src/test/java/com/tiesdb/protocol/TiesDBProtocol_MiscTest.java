package com.tiesdb.protocol;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tiesdb.protocol.TiesDBProtocol;
import com.tiesdb.protocol.Version;

@DisplayName("TiesDBProtocol Miscelaneous Test")
public class TiesDBProtocol_MiscTest {

	@Test
	@DisplayName("Protocol Sorting")
	void testProtocolSorting() {
		List<TiesDBProtocol> protocols = new ArrayList<>();
		protocols.add(newProtocol(new Version(0, 0, 1)));
		protocols.add(newProtocol(new Version(0, 0, 3)));
		protocols.add(newProtocol(new Version(2, 1, 0)));
		protocols.add(newProtocol(new Version(0, 0, 2)));
		protocols.add(newProtocol(new Version(0, 3, 0)));
		protocols.add(newProtocol(null));
		protocols.add(newProtocol(new Version(1, 3, 0)));
		protocols.add(newProtocol(new Version(0, 1, 0)));
		protocols.add(newProtocol(new Version(0, 2, 0)));
		protocols.add(newProtocol(new Version(3, 2, 0)));

		String[] p1ref = new String[] { "0.0.1", "0.0.3", "2.1", "0.0.2", "0.3", "null", "1.3", "0.1", "0.2", "3.2" };
		String[] p1 = protocols.parallelStream().map(p -> p.getVersion() == null ? "null" : p.getVersion().toString())
				.toArray(size -> new String[size]);
		assertArrayEquals(p1ref, p1);

		protocols.sort(TiesDBProtocol.PROTOCOL_COMPARATOR);
		String[] p2ref = new String[] { "0.0.1", "0.0.2", "0.0.3", "0.1", "0.2", "0.3", "1.3", "2.1", "3.2", "null" };
		String[] p2 = protocols.parallelStream().map(p -> p.getVersion() == null ? "null" : p.getVersion().toString())
				.toArray(size -> new String[size]);
		assertArrayEquals(p2ref, p2);
	}

	TiesDBProtocol newProtocol(Version ver) {
		return new TiesDBProtocol() {
			@Override
			public Version getVersion() {
				return ver;
			}
		};
	}

}
