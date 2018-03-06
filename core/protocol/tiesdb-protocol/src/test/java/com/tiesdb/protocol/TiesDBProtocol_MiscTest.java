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
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.protocol;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tiesdb.protocol.api.TiesDBProtocol;
import com.tiesdb.protocol.api.Version;

@DisplayName("TiesDBProtocol Miscelaneous Test")
public class TiesDBProtocol_MiscTest {

	@Test
	@DisplayName("Protocol Sorting")
	void testProtocolSorting() {
		List<TiesDBProtocol> protocols = new ArrayList<>();
		protocols.add(newProtocol(new Version(0, 0, 1)));
		protocols.add(newProtocol(new Version(0, 0, 3)));
		protocols.add(newProtocol(new Version(2, 1)));
		protocols.add(newProtocol(new Version(0, 0, 2)));
		protocols.add(newProtocol(new Version(0, 3, 0)));
		protocols.add(newProtocol(null));
		protocols.add(newProtocol(new Version(1, 3, 0)));
		protocols.add(newProtocol(new Version(0, 1, 0)));
		protocols.add(newProtocol(new Version(0, 2)));
		protocols.add(newProtocol(new Version(3, 2, 0)));
		protocols.add(newProtocol(new Version(4)));

		String[] p1ref = new String[] { "V0R0M1", "V0R0M3", "V2R1", "V0R0M2", "V0R3", "null", "V1R3", "V0R1", "V0R2", "V3R2", "V4R0" };
		String[] p1 = protocols.parallelStream().map(p -> p.getVersion() == null ? "null" : p.getVersion().toString())
				.toArray(size -> new String[size]);
		assertArrayEquals(p1ref, p1);

		protocols.sort(TiesDBProtocol.ProtocolComparator.FULL);
		String[] p2ref = new String[] { "V0R0M1", "V0R0M2", "V0R0M3", "V0R1", "V0R2", "V0R3", "V1R3", "V2R1", "V3R2", "V4R0", "null" };
		String[] p2 = protocols.parallelStream().map(p -> p.getVersion() == null ? "null" : p.getVersion().toString())
				.toArray(size -> new String[size]);
		assertArrayEquals(p2ref, p2);
	}

	TiesDBProtocol newProtocol(Version ver) {
		TiesDBProtocol protocol = mock(TiesDBProtocol.class);
		when(protocol.getVersion()).thenReturn(ver);
		return protocol;
	}

}
