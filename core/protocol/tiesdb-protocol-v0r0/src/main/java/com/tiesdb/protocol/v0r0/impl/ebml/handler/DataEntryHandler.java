package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import static com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLTypeContainer.*;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.v0r0.api.message.Cheque;
import com.tiesdb.protocol.v0r0.api.message.DataEntry;
import com.tiesdb.protocol.v0r0.api.message.DataEntryField;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLHeader;

public class DataEntryHandler implements TiesDBEBMLHandler<DataEntry> {

	private static final Logger LOG = LoggerFactory.getLogger(DataEntryHandler.class);

	static final ValueArrayHandler<DataEntryField> FIELDS_HANDLER = new ValueArrayHandler<DataEntryField>(//
			DataEntryField.class, FIELD, FieldHandler.INSTANCE);

	static final ValueArrayHandler<Cheque> CHEQUES_HANDLER = new ValueArrayHandler<Cheque>(//
			Cheque.class, CHEQUE, ChequeHandler.INSTANCE);

	public static final DataEntryHandler INSTANCE = new DataEntryHandler();

	private DataEntryHandler() {
	}

	@Override
	public DataEntry read(TiesDBEBMLParser parser) throws IOException {
		DataEntry result = new DataEntry();
		EBMLHeader elementHeader;
		while ((elementHeader = parser.readHeader()) != null) {
			if (ENTRY_HEADER.equals(elementHeader.getType())) {
				result.setHeader(EntryHeaderHandler.INSTANCE.read(parser));
				parser.next();
			} else if (ENTRY_FIELDS.equals(elementHeader.getType())) {
				result.setFields(FIELDS_HANDLER.read(parser));
				parser.next();
			} else if (ENTRY_CHEQUES.equals(elementHeader.getType())) {
				result.setCheques(CHEQUES_HANDLER.read(parser));
				parser.next();
			} else {
				switch (parser.getSettings().getUnexpectedPartStrategy()) {
				case ERROR:
					throw new IOException("Unexpected DataEntry part " + elementHeader.getType());
				case SKIP:
					LOG.debug("Unexpected DataEntry part {}", elementHeader.getType());
					parser.skip();
					continue;
				}
			}
		}
		return result;
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(DataEntry o) throws IOException {
		final Part<TiesDBEBMLFormatter> entryHeader = safe(EntryHeaderHandler.INSTANCE, o.getHeader());
		final Part<TiesDBEBMLFormatter> entryFields = safe(FIELDS_HANDLER, o.getFields());
		final Part<TiesDBEBMLFormatter> entryCheques = safe(CHEQUES_HANDLER, o.getCheques());
		return new Part<TiesDBEBMLFormatter>() {

			int size = -1;

			@Override
			public void write(TiesDBEBMLFormatter formatter) throws IOException {
				if (entryHeader != null) {
					formatter.newHeader(ENTRY_HEADER, entryHeader.getSize(formatter));
					entryHeader.write(formatter);
				}
				if (entryFields != null) {
					formatter.newHeader(ENTRY_FIELDS, entryFields.getSize(formatter));
					entryFields.write(formatter);
				}
				if (entryCheques != null) {
					formatter.newHeader(ENTRY_CHEQUES, entryCheques.getSize(formatter));
					entryCheques.write(formatter);
				}
			}

			@Override
			public int getSize(TiesDBEBMLFormatter formatter) throws IOException {
				if (this.size != -1) {
					return this.size;
				}
				int size = 0;
				if (entryHeader != null) {
					size += formatter.getPartSize(ENTRY_HEADER, entryHeader.getSize(formatter));
				}
				if (entryFields != null) {
					size += formatter.getPartSize(ENTRY_FIELDS, entryFields.getSize(formatter));
				}
				if (entryCheques != null) {
					size += formatter.getPartSize(ENTRY_CHEQUES, entryCheques.getSize(formatter));
				}
				return this.size = size;
			}

		};
	}
}
