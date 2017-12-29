package com.tiesdb.lib.crypto.digest;

import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.lib.crypto.digest.impl.Keccak;
import com.tiesdb.lib.crypto.digest.impl.Tiger;

public class DigestManager {
	public static final String CRYPTO_PROVIDER = "BC";

	public static final String TIGER = "TIGER";
	public static final String KECCAK = "KECCAK-256";
	public static final String KECCAK_512 = "KECCAK-512";

	public static Digest getDigest(String algorythm) {
		switch (algorythm) {
		case TIGER:
			return new Tiger();
		case KECCAK:
            return new Keccak(256);
		case KECCAK_512:
            return new Keccak(512);

		default:
			throw new IllegalArgumentException("No algorythm was found for name " + algorythm);
		}
	}
}
