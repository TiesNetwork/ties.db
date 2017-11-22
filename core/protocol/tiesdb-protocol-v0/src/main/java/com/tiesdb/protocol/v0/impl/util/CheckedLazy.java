package com.tiesdb.protocol.v0.impl.util;

import java.util.Objects;

public class CheckedLazy<E extends Throwable, T> implements CheckedSupplier<E, T> {

	private final CheckedSupplier<E, T> supplier;
	private T value;

	public CheckedLazy(CheckedSupplier<E, T> supplier) {
		Objects.requireNonNull(supplier);
		this.supplier = supplier;
	}

	@Override
	public synchronized T get() throws E {
		return value != null ? value : (value = supplier.get());
	}
}
