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
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.protocol.v0.impl;

import org.ebml.EBMLWriter;
import org.ebml.io.DataWriter;

import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel.Output;
import com.tiesdb.protocol.api.data.Element;
import com.tiesdb.protocol.api.data.ElementContainer;
import com.tiesdb.protocol.api.data.ElementWriter;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0.api.TiesElement;
import com.tiesdb.protocol.v0.exception.DataParsingException;
import com.tiesdb.protocol.v0.impl.TiesEBMLExtendedElement.ContainerElement;
import com.tiesdb.protocol.v0.impl.ebml.TiesEBMLDataWriter;

public class ElementWriterImpl extends ElementHelper implements ElementWriter<TiesElement> {

	private static final TypeVisitor<ContainerElement> MASTER = new TypeVisitor.TypeVisitorCommon<ContainerElement>() {
		@Override
		public ContainerElement visit(ContainerElement containerElement) {
			return containerElement;
		}
	};

	private final TiesEBMLDataWriter writer;

	public ElementWriterImpl(Output out) {
		this.writer = getDataWriter(out);
	}

	protected TiesEBMLDataWriter getDataWriter(Output out) {
		return new TiesEBMLDataWriter(out);
	}

	protected EBMLWriter getWriter(DataWriter wr) {
		return new EBMLWriter(wr);
	}

	@Override
	public void write(TiesElement e) {
		getWriter(writer).writeElement(toEBMLElement(e));
	}

	private org.ebml.Element toEBMLElement(TiesElement e) {
		if (e instanceof ElementContainer) {
			ContainerElement elm = asExtended(e.getType().getProtoType().getInstance()).accept(MASTER);
			for (Element child : (ElementContainer<?>) e) {
				elm.addChildElement(toEBMLElement((TiesElement) child));
			}
			return elm;
		} else if (e instanceof TiesElementValue) {
			org.ebml.Element elm = e.getType().getProtoType().getInstance();
			((TiesElementValue<?>) e).getForElementValue(asExtended(elm));
			return elm;
		} else {
			throw new DataParsingException(new TiesDBProtocolException("Unknown element type to be written"));
		}
	}

}
