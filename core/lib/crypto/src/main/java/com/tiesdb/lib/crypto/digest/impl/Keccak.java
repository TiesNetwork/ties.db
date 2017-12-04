package com.tiesdb.lib.crypto.digest.impl;

import com.tiesdb.lib.crypto.digest.api.Digest;

public final class Keccak extends org.bouncycastle.crypto.digests.KeccakDigest implements Digest {
	public Keccak() {
		super(256);
	}

	public Keccak(int bitLength) {
		super(bitLength);
	}
}