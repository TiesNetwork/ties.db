package com.tiesdb.protocol.v0r0.api.message;

import java.util.Arrays;

public class DataEntryHeader {

	private String tablespaceName;
	private String tableName;
	private Long entryVersion;
	private DataEntryType entryType;
	private Long timestamp;
	private byte[] fieldsHash; // hashTree(field.hash of ../fields)

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

	@Override
	public String toString() {
		return "DataEntryHeader [tablespaceName=" + tablespaceName + ", tableName=" + tableName + ", entryVersion=" + entryVersion
				+ ", entryType=" + entryType + ", timestamp=" + timestamp + ", fieldsHash=" + Arrays.toString(fieldsHash) + "]";
	}

}
