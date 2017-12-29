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
package com.tiesdb.protocol.v0.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tiesdb.protocol.TiesDBProtocolManager;
import com.tiesdb.protocol.api.TiesDBProtocol;
import com.tiesdb.protocol.v0.TiesDBProtocolV0;

@DisplayName("TiesDBProtocol version 0 Miscelaneous Test")
public class TiesDBProtocolV0_MiscTest {

	@Test
	@DisplayName("Protocol Service Loading")
	void testProtocolServiceLoading() {
		List<TiesDBProtocol> protocols = TiesDBProtocolManager.loadProtocols();
		assertFalse(protocols.isEmpty(), "No Protocols found");
		assertEquals(1, protocols.size());
		assertEquals(new TiesDBProtocolV0().getVersion(), protocols.get(0).getVersion());
	}

}
