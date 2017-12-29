package com.tiesdb.lib.crypto.checksum.impl;

import com.tiesdb.lib.crypto.checksum.api.Checksum;

public final class CRC32 extends java.util.zip.CRC32 implements Checksum {

	@Override
	public Checksum copy() {
		try {
			return (Checksum) clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int updateVal(int b) {
		update(b);
		return b;
	}

	@Override
	public byte updateVal(byte b) {
		update(b);
		return b;
	}

}