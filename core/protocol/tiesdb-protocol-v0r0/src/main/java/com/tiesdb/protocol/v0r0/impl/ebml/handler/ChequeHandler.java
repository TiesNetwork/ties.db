package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import static com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLTypeContainer.*;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.v0r0.api.message.Cheque;
import com.tiesdb.protocol.v0r0.api.message.part.BlockchainAddress;
import com.tiesdb.protocol.v0r0.impl.EthereumAddress;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLHeader;

public class ChequeHandler implements TiesDBEBMLHandler<Cheque> {

	private static final Logger LOG = LoggerFactory.getLogger(ChequeHandler.class);

	static final TiesDBEBMLHandler<BlockchainAddress[]> CHEQUE_RECIEPT_NODES_HANDLER = new ValueArrayDynamicHandler//
			.ValueArrayDynamicHandlerBuilder<BlockchainAddress>(BlockchainAddress.class)//
					.add(EthereumAddress.class, ADDRESS_ETHEREUM, ValueEthereumAdressHandler.INSTANCE)//
					.build();

	public static final ChequeHandler INSTANCE = new ChequeHandler();

	private ChequeHandler() {
	}

	@Override
	public Cheque read(TiesDBEBMLParser parser) throws IOException {
		Cheque result = new Cheque();
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
	public Part<TiesDBEBMLFormatter> prepare(Cheque o) throws IOException {
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
