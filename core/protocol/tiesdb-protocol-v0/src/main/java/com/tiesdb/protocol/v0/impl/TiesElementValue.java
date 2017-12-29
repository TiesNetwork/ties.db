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

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.ebml.Element;

import com.tiesdb.protocol.v0.api.TiesElement;
import com.tiesdb.protocol.v0.exception.DataParsingException;
import com.tiesdb.protocol.v0.impl.TiesEBMLExtendedElement.ContainerElement;

public abstract class TiesElementValue<T> implements TiesElement {

	private final TiesEBMLType type;

	public TiesElementValue(TiesEBMLType type) {
		this.type = type;
	}

	@Override
	public TiesEBMLType getType() {
		return this.type;
	}

	public abstract T getValue();

	public abstract void setValue(T value);

	protected abstract byte[] getRawValue();

	protected abstract void setRawValue(byte[] rawValue);

	void setFromElementValue(TiesEBMLExtendedElement element) {
		setRawValue(element.accept(SET_VISITOR));
	}

	void getForElementValue(TiesEBMLExtendedElement element) {
		element.accept(GET_VISITOR).accept(getRawValue());
	}

	private static final TypeVisitor<byte[]> SET_VISITOR = new TypeVisitor.TypeVisitorCommon<byte[]>() {

		@Override
		public byte[] visit(ContainerElement element) {
			throw new DataParsingException("Expected element value type but was container type " //
					+ element.getElementType().getName(), null);
		}

		@Override
		protected <E extends Element & TiesEBMLExtendedElement> byte[] defaultValue(E element) {
			return element.getDataArray();
		}
	};

	private static final TypeVisitor<Consumer<byte[]>> GET_VISITOR = new TypeVisitor.TypeVisitorCommon<Consumer<byte[]>>() {

		@Override
		public Consumer<byte[]> visit(ContainerElement element) {
			throw new DataParsingException("Expected element value type but was container type " //
					+ element.getElementType().getName(), null);
		}

		@Override
		protected <E extends Element & TiesEBMLExtendedElement> Consumer<byte[]> defaultValue(E element) {
			return (v) -> element.setData(ByteBuffer.wrap(v));
		}
	};

}
