package com.tiesdb.lib.crypto.digest;

import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.lib.crypto.digest.impl.Tiger;

public class DigestManager {

	public static final String TIGER = "TIGER";

	public static Digest getDigest(String algorythm) {
		switch (algorythm) {
		case TIGER:
			return new Tiger();

		default:
			throw new IllegalArgumentException("No algorythm was found for name " + algorythm);
		}
	}
}
