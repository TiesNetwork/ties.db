package com.tiesdb.protocol.v0.element;

import com.tiesdb.protocol.v0.api.TiesElement;
import com.tiesdb.protocol.v0.impl.TiesEBMLType;
import com.tiesdb.protocol.v0.impl.TiesElementContainer;

public abstract class TiesDBBaseRequest extends TiesElementContainer<TiesDBBaseRequest.Part> {

	public TiesDBBaseRequest(TiesEBMLType type) {
		super(type);
	}

	private TiesDBRequestSignature signature;

	private TiesDBRequestConsistency consistency;

	public TiesDBRequestSignature getSignature() {
		return signature;
	}

	public void setSignature(TiesDBRequestSignature signature) {
		this.signature = signature;
	}

	public TiesDBRequestConsistency getConsistency() {
		return consistency;
	}

	public void setConsistency(TiesDBRequestConsistency consistency) {
		this.consistency = consistency;
	}

	protected static interface Part extends TiesElement {
		void accept(PartVisitor v);
	}

	protected static interface PartVisitor {

		void visit(TiesDBRequestSignature tiesDBRequestSignature);

		void visit(TiesDBRequestConsistency tiesDBRequestConsistency);

	}

	protected class PartAcceptor implements PartVisitor {
		@Override
		public void visit(TiesDBRequestSignature tiesDBRequestSignature) {
			if (getSignature() != null) {
				throw new IllegalStateException("Request signature is already set");
			}
			setSignature(tiesDBRequestSignature);
		}

		@Override
		public void visit(TiesDBRequestConsistency tiesDBRequestConsistency) {
			if (getConsistency() != null) {
				throw new IllegalStateException("Request consistency is already set");
			}
			setConsistency(tiesDBRequestConsistency);
		}
	}

	protected abstract PartAcceptor getAcceptor();

	@Override
	public void accept(Part element) {
		element.accept(getAcceptor());
	}

	@Override
	public ContainerIterator<Part> iterator() {
		return createIterator(getSignature(), getConsistency());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((consistency == null) ? 0 : consistency.hashCode());
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
		TiesDBBaseRequest other = (TiesDBBaseRequest) obj;
		if (consistency == null) {
			if (other.consistency != null)
				return false;
		} else if (!consistency.equals(other.consistency))
			return false;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		return true;
	}

}