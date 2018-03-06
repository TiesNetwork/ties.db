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
package com.tiesdb.protocol.v0r0.api.message;

public class DataModificationRequest implements Request {

	private RequestConsistencyLevel consistencyLevel;
	private DataEntry dataEntry;

	@Override
	public RequestConsistencyLevel getConsistencyLevel() {
		return consistencyLevel;
	}

	public void setConsistencyLevel(RequestConsistencyLevel consistencyLevel) {
		this.consistencyLevel = consistencyLevel;
	}

	public DataEntry getDataEntry() {
		return dataEntry;
	}

	public void setDataEntry(DataEntry dataEntry) {
		this.dataEntry = dataEntry;
	}

	@Override
	public String toString() {
		return "DataModificationRequest [consistencyLevel=" + consistencyLevel + ", dataEntry=" + dataEntry + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((consistencyLevel == null) ? 0 : consistencyLevel.hashCode());
		result = prime * result + ((dataEntry == null) ? 0 : dataEntry.hashCode());
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
		DataModificationRequest other = (DataModificationRequest) obj;
		if (consistencyLevel != other.consistencyLevel)
			return false;
		if (dataEntry == null) {
			if (other.dataEntry != null)
				return false;
		} else if (!dataEntry.equals(other.dataEntry))
			return false;
		return true;
	}

}
