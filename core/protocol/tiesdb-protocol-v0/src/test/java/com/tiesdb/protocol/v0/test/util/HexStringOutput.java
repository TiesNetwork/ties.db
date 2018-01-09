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
import java.io.OutputStream;
import java.io.PrintWriter;

import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.Output;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.State;

public class HexStringOutput implements Output {

	private final static char[] hexArray = "0123456789ABCDEF".toLowerCase().toCharArray();

	private final PrintWriter pw;
	private int pos = 0;
	private ByteArrayOutputStream cache = null;

	public HexStringOutput(OutputStream os) {
		this.pw = new PrintWriter(os);
	}

	@Override
	public State state() {
		return State.OPENED;
	}

	@Override
	public boolean isOpened() {
		return true;
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public int available() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int more() {
		return -1;
	}

	@Override
	public int skip(int len) {
		pw.println();
		pw.println("<------- skipped " + len + " ------->");
		for (int i = 0; i < len; i++) {
			put((byte) 0);
		}
		return len;
	}

	@Override
	public boolean isFinished() {
		return false;
	}

	@Override
	public void put(byte b) {
		pos++;
		int v = b & 0xFF;
		pw.print(hexArray[v >>> 4]);
		pw.print(hexArray[v & 0x0F]);
		if (pos % 16 == 0) {
			pw.println();
		} else if (pos % 2 == 0) {
			pw.print(' ');
		}
		pw.flush();
	}

	private void put(byte[] buf) {
		for (int i = 0; i < buf.length; i++) {
			put(buf[i]);
		}
		pw.flush();
	}

	@Override
	public void cacheStart() {
		if (isCaching()) {
			return;
		}
		cache = new ByteArrayOutputStream();
		pw.println();
		pw.println("<------- cache start ------->");
	}

	@Override
	public void cacheClear() {
		if (!isCaching()) {
			return;
		}
		pw.println();
		pw.println("<------- cache clear -------:");
		put(cache.toByteArray());
		cache = null;
		pw.println(":------- cache clear ------->");
	}

	@Override
	public void cacheFlush() {
		if (!isCaching()) {
			return;
		}
		put(cache.toByteArray());
		this.cache = null;
		pw.println();
		pw.println("<------- cache flush ------->");
	}

	@Override
	public boolean isCaching() {
		return cache != null;
	}

	@Override
	public void close() {
		System.out.println("Can't close fake output");
	}

}
