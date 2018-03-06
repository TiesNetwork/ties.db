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
package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import static com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLTypeContainer.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SignatureException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.lib.crypto.digest.DigestManager;
import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.lib.crypto.ecc.signature.ECKey;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.api.message.BlockchainAddress;
import com.tiesdb.protocol.v0r0.api.message.Cheque;
import com.tiesdb.protocol.v0r0.api.message.DataEntry;
import com.tiesdb.protocol.v0r0.api.message.DataEntryField;
import com.tiesdb.protocol.v0r0.api.message.DataEntryHeader;
import com.tiesdb.protocol.v0r0.api.message.EthereumAddress;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLHeader;
import one.utopic.sparse.ebml.EBMLWriter.Part;

public class DataEntryHandler implements TiesDBEBMLHandler<DataEntry> {

	private static final Logger LOG = LoggerFactory.getLogger(DataEntryHandler.class);

	static final ValueArrayHandler<DataEntryField> FIELDS_HANDLER = new ValueArrayHandler<DataEntryField>(//
			DataEntryField.class, FIELD, FieldHandler.INSTANCE);

	static final ValueArrayHookedHandler<Cheque> CHEQUES_HANDLER = new ValueArrayHookedHandler<Cheque>(//
			Cheque.class, CHEQUE, ChequeHandler.INSTANCE) {

		@Override
		public Cheque[] read(TiesDBEBMLParser parser) throws IOException {
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				Consumer<Byte> consumer = parser.subscribeConsumer(b -> baos.write(b));
				try {
					return super.read(parser, e -> {
						e.setChequeRawBytes(baos.toByteArray());
						baos.reset();
					});
				} finally {
					parser.unsubscribeConsumer(consumer);
				}
			}
		}

	};

	public static final DataEntryHandler INSTANCE = new DataEntryHandler();

	private DataEntryHandler() {
	}

	@Override
	public DataEntry read(TiesDBEBMLParser parser) throws IOException {
		DataEntry result = new DataEntry();
		EBMLHeader elementHeader;
		byte[] dataEntryHeaderHash = null;
		byte[] dataEntrySignature = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			Consumer<Byte> consumer = parser.subscribeConsumer(b -> baos.write(b));
			try {
				while ((elementHeader = parser.readHeader()) != null) {
					if (null != consumer) {
						parser.unsubscribeConsumer(consumer);
					}
					if (ENTRY_HEADER.equals(elementHeader.getType())) {
						consumer = parser.subscribeConsumer(consumer);
						Digest digest = DigestManager.getDigest(DigestManager.KECCAK);
						Consumer<Byte> digestConsumer = parser.subscribeConsumer(b -> digest.update(b));
						try {
							DataEntryHeader header = EntryHeaderHandler.INSTANCE.read(parser);
							header.setHeaderRawBytes(baos.toByteArray());
							result.setHeader(header);
						} finally {
							parser.unsubscribeConsumer(digestConsumer);
							dataEntryHeaderHash = new byte[digest.getDigestSize()];
							digest.doFinal(dataEntryHeaderHash, 0);
						}
						parser.next();
						parser.unsubscribeConsumer(consumer);
						consumer = null;
					} else if (DATA_ENTRY_SIGNATURE.equals(elementHeader.getType())) {
						dataEntrySignature = ValueBinaryHandler.INSTANCE.read(parser);
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
					if (null != consumer) {
						consumer = parser.subscribeConsumer(consumer);
						baos.reset();
					}
				}
			} finally {
				if (null != consumer) {
					parser.unsubscribeConsumer(consumer);
				}
			}
		}
		if (dataEntrySignature != null) {
			try {
				result.setEntrySigner(new EthereumAddress(ECKey.signatureToAddressBytes(dataEntryHeaderHash, dataEntrySignature)));
			} catch (SignatureException e) {
				throw new IOException("Entry owner public key check failed: " + result, e);
			}
		}
		return result;
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(DataEntry o) throws IOException {
		final Part<TiesDBEBMLFormatter> entryHeader = safe(EntryHeaderHandler.INSTANCE, o.getHeader());
		final Part<TiesDBEBMLFormatter> entryFields = safe(FIELDS_HANDLER, o.getFields());
		final Part<TiesDBEBMLFormatter> entryCheques = safe(CHEQUES_HANDLER, o.getCheques());
		final BlockchainAddress entrySigner = o.getEntrySigner();
		return new Part<TiesDBEBMLFormatter>() {

			int size = -1;

			@Override
			public void write(TiesDBEBMLFormatter formatter) throws IOException {
				byte[] dataEntryHeaderHash = null;
				if (entryHeader != null) {
					formatter.newHeader(ENTRY_HEADER, entryHeader.getSize(formatter));
					Digest digest = DigestManager.getDigest(DigestManager.KECCAK);
					Consumer<Byte> consumer = formatter.subscribeConsumer(b -> digest.update(b));
					try {
						entryHeader.write(formatter);
					} finally {
						formatter.unsubscribeConsumer(consumer);
						dataEntryHeaderHash = new byte[digest.getDigestSize()];
						digest.doFinal(dataEntryHeaderHash, 0);
					}
				}
				if (formatter.getSignatureKey() != null) {
					if (entrySigner != null) {
						if (!new EthereumAddress(formatter.getSignatureKey().getAddress()).equals(entrySigner)) {
							throw new IOException("Write failed", new TiesDBProtocolException(
									"Entry owner address does not match the formatter public key address in: " + o));
						}
					}
					byte[] dataEntrySignatureBytes = formatter.getSignatureKey().sign(dataEntryHeaderHash).toByteArray();
					final Part<TiesDBEBMLFormatter> dataEntrySignature = safe(ValueBinaryHandler.INSTANCE, dataEntrySignatureBytes);
					formatter.newHeader(DATA_ENTRY_SIGNATURE, dataEntrySignature.getSize(formatter));
					dataEntrySignature.write(formatter);
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
				if (formatter.getSignatureKey() != null) {
					size += formatter.getPartSize(DATA_ENTRY_SIGNATURE, 65);
				}
				return this.size = size;
			}

		};
	}
}
