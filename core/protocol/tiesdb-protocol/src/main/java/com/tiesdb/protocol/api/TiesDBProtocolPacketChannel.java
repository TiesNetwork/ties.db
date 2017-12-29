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
 * with Ties.DB project.If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.protocol.api;

public interface TiesDBProtocolPacketChannel {

	enum State {
		OPENING, OPENED, FINISHED, CLOSING, CLOSED
	}

	interface Input {

		State state();

		boolean isOpened();

		boolean isClosed();

		boolean isFinished();

		void close();

		int available();

		int more();

		byte get();

		int skip(int length);

		void peekStart();

		void peekRewind();

		void peekSkip();

		boolean isPeeking();

	}

	interface Output {

		State state();

		boolean isOpened();

		boolean isClosed();

		boolean isFinished();

		void close();

		int available();

		int more();

		void put(byte b);

		int skip(int length);

		void cacheStart();

		void cacheClear();

		void cacheFlush();

		boolean isCaching();
	}

	Input getInput();

	Output getOutput();
}
