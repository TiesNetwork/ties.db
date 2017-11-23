package com.tiesdb.protocol.v0.impl.context;

import com.tiesdb.protocol.v0.api.context.Context;
import com.tiesdb.protocol.v0.impl.TiesDBProtocolParser;
import com.tiesdb.protocol.v0.util.Synchronized;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.Input;

abstract class ContextRoaming {

	protected abstract Synchronized<Input> getSynchronizedInput();

	protected abstract TiesDBProtocolParser getProtocolParser();

	public abstract Context<?> blockedBy();

	public boolean isWaiting() {
		Context<?> blockedBy = blockedBy();
		return blockedBy != null && !blockedBy.isClosed();
	}
}
