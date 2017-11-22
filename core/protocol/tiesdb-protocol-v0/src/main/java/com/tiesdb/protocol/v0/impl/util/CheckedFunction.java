package com.tiesdb.protocol.v0.impl.util;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction<E extends Throwable, T, R> {

	R apply(T arguments) throws E;

	default <V> CheckedFunction<E, T, V> andThen(Function<? super R, ? extends V> after) throws E {
		Objects.requireNonNull(after);
		return (T t) -> after.apply(apply(t));
	}

	default <V> CheckedFunction<E, V, R> compose(Function<? super V, ? extends T> before) throws E {
		Objects.requireNonNull(before);
		return (V v) -> apply(before.apply(v));
	}

	default <V> CheckedFunction<? extends E, T, V> andThen(CheckedFunction<? extends E, ? super R, ? extends V> after) throws E {
		Objects.requireNonNull(after);
		return (T t) -> after.apply(apply(t));
	}

	default <V> CheckedFunction<E, V, R> compose(CheckedFunction<? extends E, ? super V, ? extends T> before) throws E {
		Objects.requireNonNull(before);
		return (V v) -> apply(before.apply(v));
	}

	static <E extends Throwable, T> CheckedFunction<E, T, T> identity() {
		return t -> t;
	}
}