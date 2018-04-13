package com.tiesdb.protocol.v0r0.util;

@FunctionalInterface
public interface CheckedConsumer<T, E extends Throwable> {
    void accept(T v) throws E;
}