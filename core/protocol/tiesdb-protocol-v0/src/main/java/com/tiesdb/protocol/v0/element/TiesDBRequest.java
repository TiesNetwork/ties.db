package com.tiesdb.protocol.v0.element;

import java.util.Iterator;

import com.tiesdb.protocol.v0.api.TiesElement;
import com.tiesdb.protocol.v0.impl.TiesEBMLType;
import com.tiesdb.protocol.v0.impl.TiesElementContainer;

public class TiesDBRequest extends TiesElementContainer<TiesDBRequest.Part> {

	private TiesDBRequestHeader header;

	public TiesDBRequest() {
		super(TiesEBMLType.Request);
	}

	public TiesDBRequestHeader getHeader() {
		return header;
	}

	public void setHeader(TiesDBRequestHeader header) {
		this.header = header;
	}

	public static interface Part extends TiesElement {
		void accept(PartVisitor v);
	}

	public static interface PartVisitor {

		void visit(TiesDBRequestHeader tiesDBRequestHeader);

	}

	@Override
	public Iterator<Part> iterator() {
		return createIterator(getHeader());
	}

	@Override
	public void accept(Part element) {
		element.accept(new PartVisitor() {
			@Override
			public void visit(TiesDBRequestHeader tiesDBRequestHeader) {
				if (getHeader() != null) {
					throw new IllegalStateException("Request header is already set");
				}
				setHeader((TiesDBRequestHeader) element);
			}
		});
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TiesDBRequest other = (TiesDBRequest) obj;
		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;
		return true;
	}

}