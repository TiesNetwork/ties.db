/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project.If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
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
