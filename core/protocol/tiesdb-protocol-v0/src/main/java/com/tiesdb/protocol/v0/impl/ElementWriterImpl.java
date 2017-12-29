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
