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

import java.util.Arrays;

public class DataEntry {

	private DataEntryHeader header;
	private BlockchainAddress entrySigner;
	private Cheque[] cheques;
	private DataEntryField[] fields;

	public DataEntryHeader getHeader() {
		return header;
	}

	public void setHeader(DataEntryHeader header) {
		this.header = header;
	}

	public Cheque[] getCheques() {
		return cheques;
	}

	public void setCheques(Cheque[] cheques) {
		this.cheques = cheques;
	}

	public DataEntryField[] getFields() {
		return fields;
	}

	public void setFields(DataEntryField[] fields) {
		this.fields = fields;
	}

	public BlockchainAddress getEntrySigner() {
		return entrySigner;
	}

	public void setEntrySigner(BlockchainAddress entrySigner) {
		this.entrySigner = entrySigner;
	}

	@Override
	public String toString() {
		return "DataEntry [header=" + header + ", entrySigner=" + entrySigner + ", cheques=" + Arrays.toString(cheques) + ", fields="
				+ Arrays.toString(fields) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(cheques);
		result = prime * result + ((entrySigner == null) ? 0 : entrySigner.hashCode());
		result = prime * result + Arrays.hashCode(fields);
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
		DataEntry other = (DataEntry) obj;
		if (!Arrays.equals(cheques, other.cheques))
			return false;
		if (entrySigner == null) {
			if (other.entrySigner != null)
				return false;
		} else if (!entrySigner.equals(other.entrySigner))
			return false;
		if (!Arrays.equals(fields, other.fields))
			return false;
		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;
		return true;
	}

}
