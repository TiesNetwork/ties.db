package com.tiesdb.protocol.v0.test.util;

import com.tiesdb.protocol.TiesDBProtocolPacketChannel.Input;
import com.tiesdb.protocol.TiesDBProtocolPacketChannel.State;

public class HexStringInput implements Input {

	private final byte[] data;
	private int next = 0;
	private int mark = -1;

	public HexStringInput(String hexString) {
		this.data = javax.xml.bind.DatatypeConverter.parseHexBinary(hexString.replace(" ", ""));
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
	public int seek(int len) {
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

}
