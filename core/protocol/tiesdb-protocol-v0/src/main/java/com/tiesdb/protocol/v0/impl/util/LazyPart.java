package com.tiesdb.protocol.v0.impl.util;

import java.util.Optional;

public class LazyPart<E extends Throwable, T> implements CheckedSupplier<E, T> {

	private final CheckedSupplier<E, T> supplier;
	private Optional<T> value;

	public LazyPart(CheckedSupplier<E, T> supplier) {
		this.supplier = supplier;
	}

	@Override
	public T get() throws E {
		return value == null ? (value = Optional.of(supplier.get())).get() : value.get();
	}

}
