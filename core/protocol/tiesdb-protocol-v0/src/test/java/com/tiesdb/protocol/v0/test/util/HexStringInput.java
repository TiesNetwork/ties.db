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
package com.tiesdb.protocol.v0.test.util;

import javax.xml.bind.DatatypeConverter;

import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.Input;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.State;

public class HexStringInput implements Input {

	private final byte[] data;
	private int next = 0;
	private int mark = -1;

	public HexStringInput(String hexString) {
		String normalizedHexString = hexString.toLowerCase().replaceAll("<.*?>|[^0123456789abcdef]", "");
		this.data = DatatypeConverter.parseHexBinary(normalizedHexString);
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
		return data.length - next;
	}

	@Override
	public int more() {
		return -1;
	}

	@Override
	public byte get() {
		return data[next++];
	}

	@Override
	public int skip(int len) {
		int available = available();
		return next <= 0 ? 0 : len > available ? (next += available) : (next += len);
	}

	@Override
	public void peekStart() {
		if (!isPeeking()) {
			mark = next;
		}
	}

	@Override
	public void peekRewind() {
		if (isPeeking()) {
			next = mark;
			mark = -1;
		}
	}

	@Override
	public void peekSkip() {
		mark = -1;
	}

	@Override
	public boolean isPeeking() {
		return mark != -1;
	}

	@Override
	public boolean isFinished() {
		return data.length == next;
	}

	@Override
	public void close() {
		System.out.println("Can't close fake input");
	}

}
