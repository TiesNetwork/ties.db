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

import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.lib.crypto.digest.DigestManager;
import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.protocol.v0r0.api.message.BlockchainAddress;
import com.tiesdb.protocol.v0r0.api.message.ChequeData;
import com.tiesdb.protocol.v0r0.api.message.EthereumAddress;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLHeader;

public class ChequeDataHandler implements TiesDBEBMLHandler<ChequeData> {

	private static final Logger LOG = LoggerFactory.getLogger(ChequeDataHandler.class);

	static final TiesDBEBMLHandler<BlockchainAddress[]> CHEQUE_RECIEPT_NODES_HANDLER = new ValueArrayDynamicHandler//
			.ValueArrayDynamicHandlerBuilder<BlockchainAddress>(BlockchainAddress.class)//
					.add(EthereumAddress.class, ADDRESS_ETHEREUM, ValueEthereumAdressHandler.INSTANCE)//
					.build();

	public static final ChequeDataHandler INSTANCE = new ChequeDataHandler();

	private ChequeDataHandler() {
	}

	@Override
	public ChequeData read(TiesDBEBMLParser parser) throws IOException {
		ChequeData result = new ChequeData();
		EBMLHeader elementHeader;
		while ((elementHeader = parser.readHeader()) != null) {
			if (CHEQUE_AMOUNT.equals(elementHeader.getType())) {
				result.setAmount(ValueBigIntegerHandler.INSTANCE.read(parser));
				parser.next();
			} else if (CHEQUE_NUMBER.equals(elementHeader.getType())) {
				result.setNumber(ValueUnsignedLongHandler.INSTANCE.read(parser));
				parser.next();
			} else if (CHEQUE_RANGE.equals(elementHeader.getType())) {
				result.setRange(ValueUUIDHandler.INSTANCE.read(parser));
				parser.next();
			} else if (CHEQUE_TIMESTAMP.equals(elementHeader.getType())) {
				result.setTimestamp(ValueSignedLongHandler.INSTANCE.read(parser));
				parser.next();
			} else if (CHEQUE_RECEIPT_NODES.equals(elementHeader.getType())) {
				result.setReceiptNodes(CHEQUE_RECIEPT_NODES_HANDLER.read(parser));
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
	public Part<TiesDBEBMLFormatter> prepare(ChequeData o) throws IOException {
		final Part<TiesDBEBMLFormatter> chequeAmount = safe(ValueBigIntegerHandler.INSTANCE, o.getAmount());
		final Part<TiesDBEBMLFormatter> chequeNumber = safe(ValueUnsignedLongHandler.INSTANCE, o.getNumber());
		final Part<TiesDBEBMLFormatter> chequeRange = safe(ValueUUIDHandler.INSTANCE, o.getRange());
		final Part<TiesDBEBMLFormatter> chequeReceiptNodes = safe(CHEQUE_RECIEPT_NODES_HANDLER, o.getReceiptNodes());
		final Part<TiesDBEBMLFormatter> chequeTimestamp = safe(ValueSignedLongHandler.INSTANCE, o.getTimestamp());
		return new Part<TiesDBEBMLFormatter>() {

			@Override
			public void write(TiesDBEBMLFormatter formatter) throws IOException {
				if (chequeAmount != null) {
					formatter.newHeader(CHEQUE_AMOUNT, chequeAmount.getSize(formatter));
					chequeAmount.write(formatter);
				}
				if (chequeNumber != null) {
					formatter.newHeader(CHEQUE_NUMBER, chequeNumber.getSize(formatter));
					chequeNumber.write(formatter);
				}
				if (chequeRange != null) {
					formatter.newHeader(CHEQUE_RANGE, chequeRange.getSize(formatter));
					chequeRange.write(formatter);
				}
				if (chequeReceiptNodes != null) {
					formatter.newHeader(CHEQUE_RECEIPT_NODES, chequeReceiptNodes.getSize(formatter));
					chequeReceiptNodes.write(formatter);
				}
				if (chequeTimestamp != null) {
					formatter.newHeader(CHEQUE_TIMESTAMP, chequeTimestamp.getSize(formatter));
					chequeTimestamp.write(formatter);
				}
			}

			@Override
			public int getSize(TiesDBEBMLFormatter formatter) throws IOException {
				int size = 0;
				if (chequeAmount != null) {
					size += formatter.getPartSize(CHEQUE_AMOUNT, chequeAmount.getSize(formatter));
				}
				if (chequeNumber != null) {
					size += formatter.getPartSize(CHEQUE_NUMBER, chequeNumber.getSize(formatter));
				}
				if (chequeRange != null) {
					size += formatter.getPartSize(CHEQUE_RANGE, chequeRange.getSize(formatter));
				}
				if (chequeReceiptNodes != null) {
					size += formatter.getPartSize(CHEQUE_RECEIPT_NODES, chequeReceiptNodes.getSize(formatter));
				}
				if (chequeTimestamp != null) {
					size += formatter.getPartSize(CHEQUE_TIMESTAMP, chequeTimestamp.getSize(formatter));
				}
				return size;
			}

		};
	}
}
