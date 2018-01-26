package com.tiesdb.protocol.v0r0;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.PacketOutput;
import com.tiesdb.protocol.v0r0.impl.ProtocolHelper;
import com.tiesdb.protocol.api.Version;

class TiesDBProtocolV0R0Base {

	private static final Logger LOG = LoggerFactory.getLogger(TiesDBProtocolV0R0Base.class);

	protected final ProtocolHelper protocolHelper;

	public TiesDBProtocolV0R0Base(ProtocolHelper protocolHelper) {
		this.protocolHelper = protocolHelper != null ? protocolHelper : getDefaultProtocolHelper();
	}

	protected ProtocolHelper getDefaultProtocolHelper() {
		LOG.debug("Using default ProtocolHelper");
		return ProtocolHelper.Default.INSTANCE;
	}

	protected PacketOutput writeHeader(PacketOutput output, Version version) throws IOException {
		protocolHelper.writePacketHeader(version, output);
		return output;
	}
}
