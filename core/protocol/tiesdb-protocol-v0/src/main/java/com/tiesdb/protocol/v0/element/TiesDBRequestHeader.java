package com.tiesdb.protocol.v0.element;

import java.util.Iterator;

import com.tiesdb.protocol.v0.api.TiesElement;
import com.tiesdb.protocol.v0.impl.TiesEBMLType;
import com.tiesdb.protocol.v0.impl.TiesElementContainer;

public class TiesDBRequestHeader extends TiesElementContainer<TiesDBRequestHeader.Part> implements TiesDBRequest.Part {

	private TiesDBRequestSignature signature;

	public TiesDBRequestHeader() {
		super(TiesEBMLType.RequestHeader);
	}

	public TiesDBRequestSignature getSignature() {
		return signature;
	}

	public void setSignature(TiesDBRequestSignature tiesDBRequestSignature) {
		this.signature = tiesDBRequestSignature;
	}

	public static interface Part extends TiesElement {
		void accept(PartVisitor v);
	}

	public static interface PartVisitor {

		void visit(TiesDBRequestSignature tiesDBRequestSignature);

	}

	@Override
	public Iterator<Part> iterator() {
		return createIterator(getSignature());
	}

	@Override
	public void accept(Part element) {
		element.accept(new PartVisitor() {

			@Override
			public void visit(TiesDBRequestSignature tiesDBRequestSignature) {
				if (getSignature() != null) {
					throw new IllegalStateException("RequestHeader signature is already set");
				}
				setSignature(tiesDBRequestSignature);
			}

		});
	}

	@Override
	public void accept(TiesDBRequest.PartVisitor v) {
		v.visit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((signature == null) ? 0 : signature.hashCode());
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
		TiesDBRequestHeader other = (TiesDBRequestHeader) obj;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		return true;
	}

}