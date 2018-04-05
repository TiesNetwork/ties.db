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
package com.tiesdb.lib.crypto.encoder;

import com.tiesdb.lib.crypto.encoder.api.Encoder;
import com.tiesdb.lib.crypto.encoder.impl.Base16Encoder;
import com.tiesdb.lib.crypto.encoder.impl.Base32Encoder;
import com.tiesdb.lib.crypto.encoder.impl.Base32HexEncoder;
import com.tiesdb.lib.crypto.encoder.impl.Base64Encoder;
import com.tiesdb.lib.crypto.encoder.impl.CommonBaseEncoder.Padding;

public class EncoderManager {

    public static final String BASE64 = "BASE64";
    public static final String BASE32 = "BASE32";
    public static final String BASE32HEX = "BASE32HEX";
    public static final String BASE16 = "BASE16";

    // Packed padding
    public static final String BASE64_PP = "BASE64_PP";
    public static final String BASE32_PP = "BASE32_PP";
    public static final String BASE32HEX_PP = "BASE32HEX_PP";

    // No padding
    public static final String BASE64_NP = "BASE64_NP";
    public static final String BASE32_NP = "BASE32_NP";
    public static final String BASE32HEX_NP = "BASE32HEX_NP";

    public static Encoder getEncoder(String alg) {
        switch (alg) {
        case BASE64:
            return new Base64Encoder();
        case BASE32:
            return new Base32Encoder();
        case BASE32HEX:
            return new Base32HexEncoder();
        case BASE16:
            return new Base16Encoder();
        case BASE64_PP:
            return new Base64Encoder(Padding.PACKED);
        case BASE32_PP:
            return new Base32Encoder(Padding.PACKED);
        case BASE32HEX_PP:
            return new Base32HexEncoder(Padding.PACKED);
        case BASE64_NP:
            return new Base64Encoder(Padding.NO_PADDING);
        case BASE32_NP:
            return new Base32Encoder(Padding.NO_PADDING);
        case BASE32HEX_NP:
            return new Base32HexEncoder(Padding.NO_PADDING);
        default:
            throw new IllegalArgumentException("No encoder was found for name " + alg);
        }
    }

}
