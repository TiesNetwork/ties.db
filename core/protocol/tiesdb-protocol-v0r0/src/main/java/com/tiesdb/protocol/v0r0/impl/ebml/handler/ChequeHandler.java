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
import com.tiesdb.protocol.v0r0.api.message.EthereumAddress;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;

import one.utopic.sparse.ebml.EBMLHeader;

public class ChequeHandler implements TiesDBEBMLHandler<Cheque> {

	private static final Logger LOG = LoggerFactory.getLogger(ChequeHandler.class);

	public static final ChequeHandler INSTANCE = new ChequeHandler();

	private ChequeHandler() {
	}

	@Override
	public Cheque read(TiesDBEBMLParser parser) throws IOException {
		Cheque result = new Cheque();
		EBMLHeader elementHeader;
		byte[] chequeDataHash = null;
		byte[] chequeDataSignature = null;
		while ((elementHeader = parser.readHeader()) != null) {
			if (CHEQUE_DATA.equals(elementHeader.getType())) {
				Digest digest = DigestManager.getDigest(DigestManager.KECCAK);
				Consumer<Byte> consumer = parser.subscribeConsumer(b -> digest.update(b));
				try {
					result.setChequeData(ChequeDataHandler.INSTANCE.read(parser));
				} finally {
					parser.unsubscribeConsumer(consumer);
					chequeDataHash = new byte[digest.getDigestSize()];
					digest.doFinal(chequeDataHash, 0);
				}
				parser.next();
			} else if (CHEQUE_SIGNATURE.equals(elementHeader.getType())) {
				chequeDataSignature = ValueBinaryHandler.INSTANCE.read(parser);
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
		if (chequeDataSignature != null) {
			try {
				result.setChequeSigner(new EthereumAddress(ECKey.signatureToAddressBytes(chequeDataHash, chequeDataSignature)));
			} catch (SignatureException e) {
				throw new IOException("Cheque owner public key check failed: " + result, e);
			}
		}
		return result;
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(Cheque o) throws IOException {
		final Part<TiesDBEBMLFormatter> chequeData = safe(ChequeDataHandler.INSTANCE, o.getChequeData());
		final BlockchainAddress chequeSigner = o.getChequeSigner();
		return new Part<TiesDBEBMLFormatter>() {
			@Override
			public void write(TiesDBEBMLFormatter formatter) throws IOException {
				byte[] chequeDataHash = null;
				if (chequeData != null) {
					formatter.newHeader(CHEQUE_DATA, chequeData.getSize(formatter));
					Digest digest = DigestManager.getDigest(DigestManager.KECCAK);
					Consumer<Byte> consumer = formatter.subscribeConsumer(b -> digest.update(b));
					try {
						chequeData.write(formatter);
					} finally {
						formatter.unsubscribeConsumer(consumer);
						chequeDataHash = new byte[digest.getDigestSize()];
						digest.doFinal(chequeDataHash, 0);
					}
				}
				if (formatter.getSignatureKey() != null) {
					if (chequeSigner != null) {
						if (!new EthereumAddress(formatter.getSignatureKey().getAddress()).equals(chequeSigner)) {
							throw new IOException("Write failed", new TiesDBProtocolException(
									"Cheque owner address does not match the formatter public key address in: " + o));
						}
					}
					byte[] chequeDataSignatureBytes = formatter.getSignatureKey().sign(chequeDataHash).toByteArray();
					final Part<TiesDBEBMLFormatter> chequeDataSignature = safe(ValueBinaryHandler.INSTANCE, chequeDataSignatureBytes);
					formatter.newHeader(CHEQUE_SIGNATURE, chequeDataSignature.getSize(formatter));
					chequeDataSignature.write(formatter);
				}
			}

			@Override
			public int getSize(TiesDBEBMLFormatter formatter) throws IOException {
				int size = 0;
				if (chequeData != null) {
					size += formatter.getPartSize(CHEQUE_DATA, chequeData.getSize(formatter));
				}
				if (formatter.getSignatureKey() != null) {
					size += formatter.getPartSize(CHEQUE_SIGNATURE, 65);
				}
				return size;
			}

		};
	}
}
