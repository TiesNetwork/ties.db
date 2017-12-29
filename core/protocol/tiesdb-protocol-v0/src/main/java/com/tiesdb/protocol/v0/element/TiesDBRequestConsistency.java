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

import com.tiesdb.protocol.v0.api.ConsistencyLevel;
import com.tiesdb.protocol.v0.impl.TiesEBMLType;
import com.tiesdb.protocol.v0.impl.TiesElementValue;

public class TiesDBRequestConsistency extends TiesElementValue<ConsistencyLevel> implements TiesDBBaseRequest.Part {

	private ConsistencyLevel value;

	public TiesDBRequestConsistency() {
		super(TiesEBMLType.RequestConsistency);
	}

	@Override
	public ConsistencyLevel getValue() {
		return value;
	}

	@Override
	public void setValue(ConsistencyLevel value) {
		this.value = value;
	}

	@Override
	protected byte[] getRawValue() {
		return value != null ? new byte[] { value.getCode() } : new byte[] {};
	}

	@Override
	protected void setRawValue(byte[] rawValue) {
		if (rawValue.length > 0) {
			this.value = ConsistencyLevel.getByCode(rawValue[0]);
		}
	}

	@Override
	public void accept(TiesDBBaseRequest.PartVisitor v) {
		v.visit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		TiesDBRequestConsistency other = (TiesDBRequestConsistency) obj;
		if (value != other.value)
			return false;
		return true;
	}

}