package com.tiesdb.protocol.v0r0.reader;

import java.util.function.Consumer;

import com.tiesdb.lib.crypto.digest.api.Digest;

class DigestCalculator {

    private final Digest fieldDigest;
    private final Consumer<Byte> fieldHashListener;

    DigestCalculator(Digest fieldDigest) {
        this.fieldDigest = fieldDigest;
        this.fieldHashListener = fieldDigest::update;
    }

    public Digest getFieldDigest() {
        return fieldDigest;
    }

    public Consumer<Byte> getFieldHashListener() {
        return fieldHashListener;
    }

}