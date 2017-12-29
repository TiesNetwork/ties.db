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
