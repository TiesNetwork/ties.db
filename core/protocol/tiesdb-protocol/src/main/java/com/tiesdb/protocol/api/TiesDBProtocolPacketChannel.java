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
package com.tiesdb.protocol.api;

import one.utopic.abio.api.Closable;
import one.utopic.abio.api.Finishable;
import one.utopic.abio.api.Skippable;
import one.utopic.abio.api.Stateful;
import one.utopic.abio.api.input.Input;
import one.utopic.abio.api.output.Flushable;
import one.utopic.abio.api.output.Output;

public interface TiesDBProtocolPacketChannel {

	enum State {
		OPENING, OPENED, FINISHED, CLOSING, CLOSED
	}

	interface PacketInput extends Input, Stateful, Skippable, Closable {

		@Deprecated
		int more();

		@Deprecated
		void peekStart();

		@Deprecated
		void peekRewind();

		@Deprecated
		void peekSkip();

		@Deprecated
		boolean isPeeking();

	}

	interface PacketOutput extends Output, Stateful, Skippable, Closable, Flushable {

		@Deprecated
		int more();

		@Deprecated
		void cacheStart();

		@Deprecated
		void cacheClear();

		@Deprecated
		void cacheFlush();

		@Deprecated
		boolean isCaching();
	}

	PacketInput getInput();

	PacketOutput getOutput();
}
