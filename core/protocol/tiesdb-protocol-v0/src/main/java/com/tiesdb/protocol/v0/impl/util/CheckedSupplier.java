package com.tiesdb.protocol.v0.impl.util;

@FunctionalInterface
public interface CheckedSupplier<E extends Throwable, T> {
	T get() throws E;
}