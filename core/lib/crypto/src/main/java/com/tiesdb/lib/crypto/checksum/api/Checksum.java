package com.tiesdb.lib.crypto.checksum.api;

public interface Checksum extends Cloneable {

	public void update(int b);

	public void update(byte[] b, int off, int len);

	public long getValue();

	public void reset();

	public Checksum copy();
}
