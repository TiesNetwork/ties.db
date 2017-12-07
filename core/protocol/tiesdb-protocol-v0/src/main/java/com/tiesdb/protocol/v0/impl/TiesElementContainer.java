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
	protected final Iterator<E> createIterator(E... values) {
		return new ContainerIterator<E>(values);

	}

	private static class ContainerIterator<E extends TiesElement> implements Iterator<E> {

		private final E[] values;
		private int cursor = 0;

		private ContainerIterator(E[] values) {
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
}
