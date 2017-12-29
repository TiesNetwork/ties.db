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
package com.tiesdb.protocol.v0.element;

import com.tiesdb.protocol.v0.impl.TiesEBMLType;

public class TiesDBModificationRequest extends TiesDBBaseRequest {

	private TiesDBEntry entry;

	public TiesDBModificationRequest() {
		super(TiesEBMLType.ModificationRequest);
	}

	public TiesDBEntry getEntry() {
		return entry;
	}

	public void setEntry(TiesDBEntry entry) {
		this.entry = entry;
	}

	protected static interface Part extends TiesDBBaseRequest.Part {

		void accept(PartVisitor v);

		@Override
		default void accept(TiesDBBaseRequest.PartVisitor v) {
			if (v instanceof PartVisitor) {
				accept((PartVisitor) v);
				return;
			}
			throw new IllegalArgumentException("TiesDBModificationRequest part visitor is required");
		}

	}

	protected static interface PartVisitor extends TiesDBBaseRequest.PartVisitor {

		void visit(TiesDBEntry tiesDBEntry);

	}

	protected class PartAcceptor extends TiesDBBaseRequest.PartAcceptor implements PartVisitor {

		@Override
		public void visit(TiesDBEntry tiesDBEntry) {
			if (getEntry() != null) {
				throw new IllegalStateException("Request entry is already set");
			}
			setEntry(tiesDBEntry);
		}

	}

	@Override
	protected TiesDBBaseRequest.PartAcceptor getAcceptor() {
		return new PartAcceptor();
	}

	@Override
	public ContainerIterator<TiesDBBaseRequest.Part> iterator() {
		return createIterator(super.iterator(), getEntry());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((entry == null) ? 0 : entry.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TiesDBModificationRequest other = (TiesDBModificationRequest) obj;
		if (entry == null) {
			if (other.entry != null)
				return false;
		} else if (!entry.equals(other.entry))
			return false;
		return true;
	}
}
