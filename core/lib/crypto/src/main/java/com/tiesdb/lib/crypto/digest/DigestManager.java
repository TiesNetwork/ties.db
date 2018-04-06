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
package com.tiesdb.lib.crypto.digest;

import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.lib.crypto.digest.impl.Keccak;
import com.tiesdb.lib.crypto.digest.impl.Tiger;

public class DigestManager {

    public static final String TIGER = "TIGER";
    public static final String KECCAK = "KECCAK";
    public static final String KECCAK_224 = "KECCAK-224";
    public static final String KECCAK_256 = "KECCAK-256";
    public static final String KECCAK_384 = "KECCAK-384";
    public static final String KECCAK_512 = "KECCAK-512";

    public static Digest getDigest(String algorythm) {
        switch (algorythm) {
        case TIGER:
            return new Tiger();
        case KECCAK:
        case KECCAK_256:
            return new Keccak(256);
        case KECCAK_224:
            return new Keccak(224);
        case KECCAK_384:
            return new Keccak(384);
        case KECCAK_512:
            return new Keccak(512);

        default:
            throw new IllegalArgumentException("No algorythm was found for name " + algorythm);
        }
    }
}
