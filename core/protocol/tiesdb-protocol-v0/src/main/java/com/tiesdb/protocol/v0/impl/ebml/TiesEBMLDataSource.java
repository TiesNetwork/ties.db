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
package com.tiesdb.protocol.v0.impl.ebml;

import java.nio.ByteBuffer;

import org.ebml.io.DataSource;

import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.Input;

public class TiesEBMLDataSource implements DataSource {

	private final Input input;
	private long pointer = 0;

	public TiesEBMLDataSource(Input input) {
		this.input = input;
	}

	@Override
	public long length() {
		return -1;
	}

	@Override
	public long getFilePointer() {
		return pointer;
	}

	@Override
	public boolean isSeekable() {
		return true;
	}

	@Override
	public long seek(long pos) {
		if (pos < pointer) {
			throw new IllegalArgumentException("Can't seek backwards"); // TODO Exception
		}
		long offset = pos - pointer;
		while (offset > Integer.MAX_VALUE) {
			pointer += input.skip(Integer.MAX_VALUE);
			offset -= Integer.MAX_VALUE;
		}
		pointer += input.skip((int) offset);
		return pointer;
	}

	public boolean isFinished() {
		return input.isFinished();
	}

	@Override
	public byte readByte() {
		pointer++;
		return input.get();
	}

	@Override
	public int read(ByteBuffer buff) {
		int pos = buff.position();
		while (buff.hasRemaining()) {
			buff.put(readByte());
		}
		return buff.position() - pos;
	}

	@Override
	public long skip(long offset) {
		long pointer = this.pointer;
		return seek(pointer + offset) - pointer;
	}

}
