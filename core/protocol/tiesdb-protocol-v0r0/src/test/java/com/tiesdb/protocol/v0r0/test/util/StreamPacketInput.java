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
package com.tiesdb.protocol.v0r0.test.util;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.PacketInput;

public class StreamPacketInput implements PacketInput {

	private final BufferedInputStream is;
	private int buf = -2;
	private Integer oldBuf = null;

	public StreamPacketInput(InputStream is) {
		this.is = new BufferedInputStream(is);
	}

	@Override
	public boolean isFinished() {
		if (buf == -2) {
			advance();
		}
		return buf < 0;
	}

	private void advance() {
		try {
			buf = is.read();
		} catch (IOException e) {
			e.printStackTrace();
			buf = -3;
		}
	}

	@Override
	public byte readByte() throws IOException {
		if (isFinished()) {
			throw new EOFException();
		}
		int result = buf;
		advance();
		return (byte) result;
	}

	@Override
	public ResourceState getState() {
		switch (buf) {
		case -1:
			return ResourceState.FINISHED;
		case -3:
			return ResourceState.ERROR;
		case -4:
			return ResourceState.CLOSED;
		default:
			return ResourceState.READY;
		}
	}

	@Override
	public boolean isOpened() {
		return getState().isOpened();
	}

	@Override
	public boolean isReady() {
		return getState().isReady();
	}

	@Override
	public boolean isBusy() {
		return getState().isBusy();
	}

	@Override
	public boolean isClosed() {
		return getState().isClosed();
	}

	@Override
	public boolean isReleased() {
		return getState().isReleased();
	}

	@Override
	public boolean isError() {
		return getState().isError();
	}

	@Override
	public int skip(int byteCount) throws IOException {
		if (byteCount < 0) {
			throw new IOException("Can't skip negative number of bytes");
		}
		if (byteCount == 1) {
			advance();
			return 1;
		}
		buf = -2;
		return (int) is.skip(byteCount - 1);
	}

	@Override
	public void close() throws IOException {
		try {
			is.close();
			buf = -4;
		} catch (Exception e) {
			e.printStackTrace();
			buf = -3;
		}
	}

	@Override
	public void peekStart() {
		if (isPeeking()) {
			throw new IllegalStateException("Peeking already");
		}
		oldBuf = buf;
		is.mark(Integer.MAX_VALUE);
	}

	@Override
	public void peekRewind() {
		if (!isPeeking()) {
			throw new IllegalStateException("Peeking required");
		}
		try {
			is.reset();
			is.mark(0);
			buf = oldBuf;
			oldBuf = null;
		} catch (IOException e) {
			e.printStackTrace();
			buf = -3;
		}
	}

	@Override
	public void peekSkip() {
		if (!isPeeking()) {
			throw new IllegalStateException("Peeking required");
		}
		is.mark(0);
		oldBuf = null;

	}

	@Override
	public boolean isPeeking() {
		return null != oldBuf;
	}

}
