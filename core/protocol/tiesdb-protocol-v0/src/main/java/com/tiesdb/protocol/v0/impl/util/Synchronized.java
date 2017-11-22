package com.tiesdb.protocol.v0.impl.util;

import java.util.Objects;

public class Synchronized<T> {

	private final T obj;

	public Synchronized(T obj) {
		Objects.requireNonNull(obj);
		this.obj = obj;
	}

	public synchronized <E extends Throwable, R> R sync(CheckedFunction<E, T, R> function) throws E {
		Objects.requireNonNull(function);
		return function.apply(obj);
	}
}
