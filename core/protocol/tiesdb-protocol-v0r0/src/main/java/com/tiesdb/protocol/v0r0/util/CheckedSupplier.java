package com.tiesdb.protocol.v0r0.util;

@FunctionalInterface
public interface CheckedSupplier<T, E extends Throwable> {
    T get() throws E;
}