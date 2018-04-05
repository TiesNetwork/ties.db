package com.tiesdb.lib.crypto.encoder.impl;

public class Base16Encoder extends CommonBaseEncoder {

    private static final byte[] BASE16_CODING_TABLE = { //
            (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', //
            (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', //
    };

    public Base16Encoder() {
        super(BASE16_CODING_TABLE);
    }

}
