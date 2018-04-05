package com.tiesdb.lib.crypto.encoder.impl;

public class Base32Encoder extends CommonBaseEncoder {

    private static final byte[] BASE32_CODING_TABLE = { //
            (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J',
            (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T',
            (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', //
            (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', //
    };

    public Base32Encoder(byte singlePadding, byte doublePadding, Padding paddingStrategy) {
        super(singlePadding, doublePadding, paddingStrategy, BASE32_CODING_TABLE);
    }

    public Base32Encoder() {
        super(BASE32_CODING_TABLE);
    }

    public Base32Encoder(Padding paddingStrategy) {
        super(paddingStrategy, BASE32_CODING_TABLE);
    }

}
