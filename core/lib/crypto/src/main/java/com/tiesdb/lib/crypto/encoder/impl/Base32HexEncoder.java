package com.tiesdb.lib.crypto.encoder.impl;

public class Base32HexEncoder extends CommonBaseEncoder {

    private static final byte[] BASE32_CODING_TABLE = { //
            (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', //
            (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J',
            (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T',
            (byte) 'U', (byte) 'V', //
    };

    public Base32HexEncoder(byte singlePadding, byte doublePadding, Padding paddingStrategy) {
        super(singlePadding, doublePadding, paddingStrategy, BASE32_CODING_TABLE);
    }

    public Base32HexEncoder() {
        super(BASE32_CODING_TABLE);
    }

    public Base32HexEncoder(Padding paddingStrategy) {
        super(paddingStrategy, BASE32_CODING_TABLE);
    }

}
