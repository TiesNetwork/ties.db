package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import static com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLTypeContainer.*;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.v0r0.api.message.DataEntryHeader;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLHeader;

public class EntryHeaderHandler implements TiesDBEBMLHandler<DataEntryHeader> {

	private static final Logger LOG = LoggerFactory.getLogger(EntryHeaderHandler.class);

	public static final EntryHeaderHandler INSTANCE = new EntryHeaderHandler();

	private EntryHeaderHandler() {
	}

	@Override
	public DataEntryHeader read(TiesDBEBMLParser parser) throws IOException {
		DataEntryHeader result = new DataEntryHeader();
		EBMLHeader elementHeader;
		while ((elementHeader = parser.readHeader()) != null) {
			if (ENTRY_TYPE.equals(elementHeader.getType())) {
				result.setEntryType(EntryTypeHandler.INSTANCE.read(parser));
				parser.next();
			} else if (ENTRY_VERSION.equals(elementHeader.getType())) {
				result.setEntryVersion(ValueUnsignedLongHandler.INSTANCE.read(parser));
				parser.next();
			} else if (ENTRY_FIELDS_HASH.equals(elementHeader.getType())) {
				result.setFieldsHash(ValueBinaryHandler.INSTANCE.read(parser));
				parser.next();
			} else if (ENTRY_TABLE_NAME.equals(elementHeader.getType())) {
				result.setTableName(ValueUTF8StringHandler.INSTANCE.read(parser));
				parser.next();
			} else if (ENTRY_TABLESPACE_NAME.equals(elementHeader.getType())) {
				result.setTablespaceName(ValueUTF8StringHandler.INSTANCE.read(parser));
				parser.next();
			} else if (ENTRY_TIMESTAMP.equals(elementHeader.getType())) {
				result.setTimestamp(ValueSignedLongHandler.INSTANCE.read(parser));
				parser.next();
			} else {
				switch (parser.getSettings().getUnexpectedPartStrategy()) {
				case ERROR:
					throw new IOException("Unexpected DataEntryHeader part " + elementHeader.getType());
				case SKIP:
					LOG.debug("Unexpected DataEntryHeader part {}", elementHeader.getType());
					parser.skip();
					continue;
				}
			}
		}
		return result;
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(DataEntryHeader o) throws IOException {
		final Part<TiesDBEBMLFormatter> entryType = safe(EntryTypeHandler.INSTANCE, o.getEntryType());
		final Part<TiesDBEBMLFormatter> entryVersion = safe(ValueUnsignedLongHandler.INSTANCE, o.getEntryVersion());
		final Part<TiesDBEBMLFormatter> entryFieldsHashRoot = safe(ValueBinaryHandler.INSTANCE, o.getFieldsHash());
		final Part<TiesDBEBMLFormatter> entryTableName = safe(ValueUTF8StringHandler.INSTANCE, o.getTableName());
		final Part<TiesDBEBMLFormatter> entryTablespaceName = safe(ValueUTF8StringHandler.INSTANCE, o.getTablespaceName());
		final Part<TiesDBEBMLFormatter> entryTimestamp = safe(ValueUnsignedLongHandler.INSTANCE, o.getTimestamp());
		return new Part<TiesDBEBMLFormatter>() {

			@Override
			public void write(TiesDBEBMLFormatter formatter) throws IOException {
				if (entryType != null) {
					formatter.newHeader(ENTRY_TYPE, entryType.getSize(formatter));
					entryType.write(formatter);
				}
				if (entryVersion != null) {
					formatter.newHeader(ENTRY_VERSION, entryVersion.getSize(formatter));
					entryVersion.write(formatter);
				}
				if (entryFieldsHashRoot != null) {
					formatter.newHeader(ENTRY_FIELDS_HASH, entryFieldsHashRoot.getSize(formatter));
					entryFieldsHashRoot.write(formatter);
				}
				if (entryTableName != null) {
					formatter.newHeader(ENTRY_TABLE_NAME, entryTableName.getSize(formatter));
					entryTableName.write(formatter);
				}
				if (entryTablespaceName != null) {
					formatter.newHeader(ENTRY_TABLESPACE_NAME, entryTablespaceName.getSize(formatter));
					entryTablespaceName.write(formatter);
				}
				if (entryTimestamp != null) {
					formatter.newHeader(ENTRY_TIMESTAMP, entryTimestamp.getSize(formatter));
					entryTimestamp.write(formatter);
				}
			}

			@Override
			public int getSize(TiesDBEBMLFormatter formatter) throws IOException {
				int size = 0;
				if (entryType != null) {
					size += formatter.getPartSize(ENTRY_TYPE, entryType.getSize(formatter));
				}
				if (entryVersion != null) {
					size += formatter.getPartSize(ENTRY_VERSION, entryVersion.getSize(formatter));
				}
				if (entryFieldsHashRoot != null) {
					size += formatter.getPartSize(ENTRY_FIELDS_HASH, entryFieldsHashRoot.getSize(formatter));
				}
				if (entryTableName != null) {
					size += formatter.getPartSize(ENTRY_TABLE_NAME, entryTableName.getSize(formatter));
				}
				if (entryTablespaceName != null) {
					size += formatter.getPartSize(ENTRY_TABLESPACE_NAME, entryTablespaceName.getSize(formatter));
				}
				if (entryTimestamp != null) {
					size += formatter.getPartSize(ENTRY_TIMESTAMP, entryTimestamp.getSize(formatter));
				}
				return size;
			}

		};
	}
}
