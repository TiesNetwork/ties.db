package com.tiesdb.protocol.v0r0.impl.ebml;

import one.utopic.sparse.ebml.EBMLCode;
import one.utopic.sparse.ebml.EBMLType;
import one.utopic.sparse.ebml.util.EBMLHelper;

public final class TiesDBEBMLTypeContainer {

	public static final EBMLType.RootContext ROOT_CTX = new EBMLType.RootContext();

	private static final EBMLType.SubContext REQUEST_CTX = new EBMLType.SubContext(ROOT_CTX);
	public static final EBMLType MODIFICATION_REQUEST = REQUEST_CTX.newType("ModificationRequest", newCode(0x1e544945));
	public static final EBMLType REQUEST_CONSISTENCY = REQUEST_CTX.newType("RequestConsistency", newCode(0xcc));
	static {
		ROOT_CTX.addType(MODIFICATION_REQUEST);
	}

	private static final EBMLType.SubContext DATA_ENTRY_CTX = new EBMLType.SubContext(ROOT_CTX);
	public static final EBMLType DATA_ENTRY = DATA_ENTRY_CTX.newType("DataEntry", newCode(0xee));
	static {
		ROOT_CTX.addType(DATA_ENTRY);
	}

	private static final EBMLType.SubContext DATA_CHEQUE_CTX = new EBMLType.SubContext(DATA_ENTRY_CTX);
	public static final EBMLType ENTRY_CHEQUES = DATA_CHEQUE_CTX.newType("EntryCheques", newCode(0xcc));
	public static final EBMLType CHEQUE = DATA_CHEQUE_CTX.newType("Cheque", newCode(0xc0));
	public static final EBMLType CHEQUE_AMOUNT = DATA_CHEQUE_CTX.newType("ChequeAmount", newCode(0xc1));
	public static final EBMLType CHEQUE_NUMBER = DATA_CHEQUE_CTX.newType("ChequeNumber", newCode(0xc2));
	public static final EBMLType CHEQUE_RANGE = DATA_CHEQUE_CTX.newType("ChequeRange", newCode(0xc3));
	public static final EBMLType CHEQUE_TIMESTAMP = DATA_CHEQUE_CTX.newType("ChequeTimestamp", newCode(0xc4));
	public static final EBMLType CHEQUE_RECEIPT_NODES = DATA_CHEQUE_CTX.newType("ChequeRecieptNodes", newCode(0xca));
	static {
		DATA_ENTRY_CTX.addType(ENTRY_CHEQUES);
	}

	private static final EBMLType.RootContext ADDRESS_CTX = new EBMLType.RootContext();
	public static final EBMLType ADDRESS_ETHEREUM = ADDRESS_CTX.newType("AddressEthereum", newCode(0xce));
	static {
		DATA_CHEQUE_CTX.addType(ADDRESS_ETHEREUM);
	}

	private static final EBMLType.SubContext DATA_ENTRY_HEADER_CTX = new EBMLType.SubContext(DATA_ENTRY_CTX);
	public static final EBMLType ENTRY_HEADER = DATA_ENTRY_HEADER_CTX.newType("DataEntryHeader", newCode(0xe0));
	public static final EBMLType ENTRY_TIMESTAMP = DATA_ENTRY_HEADER_CTX.newType("Timestamp", newCode(0xe1));
	public static final EBMLType ENTRY_TYPE = DATA_ENTRY_HEADER_CTX.newType("EntryType", newCode(0xe2));
	public static final EBMLType ENTRY_VERSION = DATA_ENTRY_HEADER_CTX.newType("EntryVersion", newCode(0xe3));
	public static final EBMLType ENTRY_TABLE_NAME = DATA_ENTRY_HEADER_CTX.newType("TableName", newCode(0xe4));
	public static final EBMLType ENTRY_TABLESPACE_NAME = DATA_ENTRY_HEADER_CTX.newType("TablespaceName", newCode(0xe5));
	public static final EBMLType ENTRY_FIELDS_HASH = DATA_ENTRY_HEADER_CTX.newType("FieldsHash", newCode(0xe6));
	static {
		DATA_ENTRY_CTX.addType(ENTRY_HEADER);
	}

	private static final EBMLType.SubContext DATA_ENTRY_FIELD_CTX = new EBMLType.SubContext(DATA_ENTRY_CTX);
	public static final EBMLType ENTRY_FIELDS = DATA_ENTRY_FIELD_CTX.newType("DataEntryFields", newCode(0xf0));
	public static final EBMLType FIELD = DATA_ENTRY_FIELD_CTX.newType("DataEntryField", newCode(0xf1));
	public static final EBMLType FIELD_HASH = DATA_ENTRY_FIELD_CTX.newType("FieldHash", newCode(0xf2));
	public static final EBMLType FIELD_NAME = DATA_ENTRY_FIELD_CTX.newType("FieldName", newCode(0xf3));
	static {
		DATA_ENTRY_CTX.addType(ENTRY_FIELDS);
	}

	private static final EBMLType.SubContext FIELD_VALUE_CTX = new EBMLType.SubContext(DATA_ENTRY_FIELD_CTX);
	public static final EBMLType FIELD_VALUE = FIELD_VALUE_CTX.newType("FieldValue", newCode(0x80));
	public static final EBMLType VALUE_TYPE = FIELD_VALUE_CTX.newType("ValueType", newCode(0x81));
	public static final EBMLType VALUE_DATA = FIELD_VALUE_CTX.newType("ValueData", newCode(0x82));
	static {
		DATA_ENTRY_FIELD_CTX.addType(FIELD_VALUE);
	}

	private TiesDBEBMLTypeContainer() {
	}

	private static EBMLCode newCode(long code) {
		return new EBMLCode(code > 0xFF ? EBMLHelper.longToBytes(code) : new byte[] { (byte) (0xFF & code) });
	}
}