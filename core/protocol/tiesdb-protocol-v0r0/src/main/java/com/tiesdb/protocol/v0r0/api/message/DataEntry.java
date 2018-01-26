package com.tiesdb.protocol.v0r0.api.message;

import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

public class DataEntry {

	private DataEntryHeader header;
	private byte[] headerSignature; // sig(header)
	private Cheque[] cheques;
	private byte[] chequesSignature; // sig(cheque.hash of cheques)
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

	@Override
	public String toString() {
		return "DataEntry [header=" + header + ", headerSignature="
				+ (headerSignature == null ? "null" : DatatypeConverter.printHexBinary(headerSignature)) + ", cheques="
				+ Arrays.toString(cheques) + ", chequesSignature="
				+ (chequesSignature == null ? "null" : DatatypeConverter.printHexBinary(chequesSignature)) + ", fields="
				+ Arrays.toString(fields) + "]";
	}

}
