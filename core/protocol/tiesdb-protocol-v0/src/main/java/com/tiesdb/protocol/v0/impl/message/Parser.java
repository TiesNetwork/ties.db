package com.tiesdb.protocol.v0.impl.message;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.impl.util.CheckedFunction;
import com.tiesdb.protocol.v0.impl.util.CheckedLazy;
import com.tiesdb.protocol.v0.impl.util.CheckedSupplier;
import com.tiesdb.protocol.v0.impl.util.Synchronized;

import java.util.function.Function;

import com.tiesdb.protocol.TiesDBProtocolPacketChannel;
import com.tiesdb.protocol.TiesDBProtocolPacketChannel.Input;

abstract class Parser<E extends TiesDBProtocolException, R>
		implements CheckedFunction<E, TiesDBProtocolPacketChannel.Input, R>, CheckedSupplier<E, R> {

	protected class Field<O> extends CheckedLazy<E, O> {
		public Field(CheckedSupplier<E, O> supplier) {
			super(supplier);
		}
	}

	private final Synchronized<Input> inputWrapper;

	protected Parser(Synchronized<Input> inputWrapper) {
		this.inputWrapper = inputWrapper;
	}

	protected <O> Field<O> lazy(Function<Synchronized<Input>, Parser<E, O>> pf) {
		return new Field<O>(pf.apply(inputWrapper)::get);
	}

	@Override
	public R get() throws E {
		return inputWrapper.sync(this);
	}
}