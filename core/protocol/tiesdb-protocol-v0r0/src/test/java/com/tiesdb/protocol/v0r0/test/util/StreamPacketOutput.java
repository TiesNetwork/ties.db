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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.PacketOutput;;

public class StreamPacketOutput implements PacketOutput {

	private final OutputStream bos;
	private OutputStream os;
	private boolean error;

	public StreamPacketOutput(OutputStream os) {
		this.os = this.bos = os;
	}

	@Override
	public boolean isFinished() {
		return getState().isFinished();
	}

	@Override
	public void writeByte(byte b) throws IOException {
		if (isFinished()) {
			throw new EOFException();
		}
		os.write(b);
	}

	@Override
	public ResourceState getState() {
		return error ? ResourceState.ERROR : os == null ? ResourceState.CLOSED : ResourceState.READY;
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
	public void flush() throws IOException {
		os.flush();
	}

	@Override
	public void close() throws IOException {
		try {
			os.close();
		} catch (IOException e) {
			fail();
		}
	}

	private void fail() {
		os = null;
		error = true;
	}

	@Override
	public void cacheStart() {
		if (isCaching()) {
			throw new IllegalStateException("Caching already");
		}
		os = new ByteArrayOutputStream();
	}

	@Override
	public void cacheClear() {
		if (!isCaching()) {
			throw new IllegalStateException("Caching required");
		}
		os = bos;
	}

	@Override
	public void cacheFlush() {
		if (!isCaching()) {
			throw new IllegalStateException("Caching required");
		}
		ByteArrayOutputStream baos = (ByteArrayOutputStream) os;
		os = bos;
		try {
			os.write(baos.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException("Cache flush failed", e);
		}
	}

	@Override
	public boolean isCaching() {
		return os != bos;
	}

}
