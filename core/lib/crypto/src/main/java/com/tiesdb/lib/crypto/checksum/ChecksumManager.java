package com.tiesdb.lib.crypto.checksum;

import com.tiesdb.lib.crypto.checksum.api.Checksum;
import com.tiesdb.lib.crypto.checksum.impl.CRC32;

public class ChecksumManager {

	public static final String CRC32 = "CRC32";

	public static Checksum getChecksum(String algorythm) {
		switch (algorythm) {
		case CRC32:
			return new CRC32();

		default:
			throw new IllegalArgumentException("No algorythm was found for name " + algorythm);
		}
	}
}
