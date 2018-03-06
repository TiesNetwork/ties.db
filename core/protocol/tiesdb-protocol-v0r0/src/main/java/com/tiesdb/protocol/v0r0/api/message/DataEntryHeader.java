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

import javax.xml.bind.DatatypeConverter;

public class DataEntryHeader {

	private String tablespaceName;
	private String tableName;
	private Long entryVersion;
	private DataEntryType entryType;
	private Long timestamp;
	private byte[] fieldsHash; // hashTree(field.hash of ../fields)

	private byte[] headerRawBytes;

	public String getTablespaceName() {
		return tablespaceName;
	}

	public void setTablespaceName(String tablespaceName) {
		this.tablespaceName = tablespaceName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Long getEntryVersion() {
		return entryVersion;
	}

	public void setEntryVersion(long entryVersion) {
		this.entryVersion = entryVersion;
	}

	public DataEntryType getEntryType() {
		return entryType;
	}

	public void setEntryType(DataEntryType entryType) {
		this.entryType = entryType;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public byte[] getFieldsHash() {
		return fieldsHash;
	}

	public void setFieldsHash(byte[] fieldsHash) {
		this.fieldsHash = fieldsHash;
	}

	public byte[] getHeaderRawBytes() {
		return headerRawBytes;
	}

	public void setHeaderRawBytes(byte[] headerRawBytes) {
		this.headerRawBytes = headerRawBytes;
	}

	@Override
	public String toString() {
		return "DataEntryHeader [tablespaceName=" + tablespaceName + ", tableName=" + tableName + ", entryVersion=" + entryVersion
				+ ", entryType=" + entryType + ", timestamp=" + timestamp + ", fieldsHash="
				+ (fieldsHash == null ? "null" : DatatypeConverter.printHexBinary(fieldsHash)) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entryType == null) ? 0 : entryType.hashCode());
		result = prime * result + ((entryVersion == null) ? 0 : entryVersion.hashCode());
		result = prime * result + Arrays.hashCode(fieldsHash);
		result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
		result = prime * result + ((tablespaceName == null) ? 0 : tablespaceName.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
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
		DataEntryHeader other = (DataEntryHeader) obj;
		if (entryType != other.entryType)
			return false;
		if (entryVersion == null) {
			if (other.entryVersion != null)
				return false;
		} else if (!entryVersion.equals(other.entryVersion))
			return false;
		if (!Arrays.equals(fieldsHash, other.fieldsHash))
			return false;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		if (tablespaceName == null) {
			if (other.tablespaceName != null)
				return false;
		} else if (!tablespaceName.equals(other.tablespaceName))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

}
