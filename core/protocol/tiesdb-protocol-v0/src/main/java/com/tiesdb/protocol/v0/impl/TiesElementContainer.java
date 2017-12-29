package com.tiesdb.protocol.v0.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.tiesdb.protocol.api.data.ElementContainer;
import com.tiesdb.protocol.v0.api.TiesElement;

public abstract class TiesElementContainer<E extends TiesElement> implements TiesElement, ElementContainer<E> {

	private final TiesEBMLType type;

	public TiesElementContainer(TiesEBMLType type) {
		super();
		this.type = type;
	}

	@Override
	public TiesEBMLType getType() {
		return this.type;
	}

	@SafeVarargs
	protected final ContainerIterator<E> createIterator(E... values) {
		return new ValuesIterator<E>(values);

	}

	@SafeVarargs
	protected final ContainerIterator<E> createIterator(ContainerIterator<E> it, E... values) {
		return new CompoundIterator<E>(it, values);

	}

	protected static interface ContainerIterator<E extends TiesElement> extends Iterator<E> {

	}

	private static class ValuesIterator<E extends TiesElement> implements ContainerIterator<E> {

		private final E[] values;
		private int cursor = 0;

		ValuesIterator(E[] values) {
			this.values = values;
		}

		@Override
		public boolean hasNext() {
			while (cursor < values.length && values[cursor] == null) {
				cursor++;
			}
			return cursor < values.length;
		}

		@Override
		public E next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return values[cursor++];
		}

	}

	private static class CompoundIterator<E extends TiesElement> extends ValuesIterator<E> {

		private final ContainerIterator<E> it;

		CompoundIterator(ContainerIterator<E> it, E[] values) {
			super(values);
			this.it = it;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext() || super.hasNext();
		}

		@Override
		public E next() {
			return it.hasNext() ? it.next() : super.next();
		}

	}
}
