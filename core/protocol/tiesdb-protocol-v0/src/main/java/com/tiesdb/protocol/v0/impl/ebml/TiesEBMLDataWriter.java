package com.tiesdb.protocol.v0.impl.ebml;

import java.nio.ByteBuffer;

import org.ebml.io.DataWriter;

import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.Output;

public class TiesEBMLDataWriter implements DataWriter {

	private final Output out;
	private long pointer = 0;

	public TiesEBMLDataWriter(Output out) {
		this.out = out;
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
			pointer += out.skip(Integer.MAX_VALUE);
			offset -= Integer.MAX_VALUE;
		}
		pointer += out.skip((int) offset);
		return pointer;
	}

	public boolean isFinished() {
		return out.isFinished();
	}

	@Override
	public int write(byte b) {
		out.put(b);
		return 1;
	}

	@Override
	public int write(ByteBuffer buff) {
		int pos = buff.position();
		while (buff.hasRemaining()) {
			out.put(buff.get());
		}
		return buff.position() - pos;
	}
}
