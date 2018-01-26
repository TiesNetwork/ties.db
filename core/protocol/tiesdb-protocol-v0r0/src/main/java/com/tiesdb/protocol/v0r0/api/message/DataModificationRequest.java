package com.tiesdb.protocol.v0r0.api.message;

public class DataModificationRequest {

	private RequestConsistencyLevel consistencyLevel;
	private DataEntry dataEntry;

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

}
