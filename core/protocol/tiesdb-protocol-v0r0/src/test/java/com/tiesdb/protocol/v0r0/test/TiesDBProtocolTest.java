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
package com.tiesdb.protocol.v0r0.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.bind.DatatypeConverter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tiesdb.lib.crypto.ecc.signature.ECKey;
import com.tiesdb.protocol.TiesDBProtocolManager;
import com.tiesdb.protocol.api.TiesDBProtocol;
import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.api.TiesDBProtocolV0R0Handler;
import com.tiesdb.protocol.v0r0.api.message.BlockchainAddress;
import com.tiesdb.protocol.v0r0.api.message.Cheque;
import com.tiesdb.protocol.v0r0.api.message.ChequeData;
import com.tiesdb.protocol.v0r0.api.message.DataEntry;
import com.tiesdb.protocol.v0r0.api.message.DataEntryField;
import com.tiesdb.protocol.v0r0.api.message.DataEntryHeader;
import com.tiesdb.protocol.v0r0.api.message.DataEntryType;
import com.tiesdb.protocol.v0r0.api.message.DataModificationRequest;
import com.tiesdb.protocol.v0r0.api.message.EthereumAddress;
import com.tiesdb.protocol.v0r0.api.message.FieldValue;
import com.tiesdb.protocol.v0r0.api.message.Request;
import com.tiesdb.protocol.v0r0.api.message.RequestConsistencyLevel;
import com.tiesdb.protocol.v0r0.impl.TiesDBConversationV0R0;
import com.tiesdb.protocol.v0r0.impl.TiesDBProtocolV0R0;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLTypeContainer;
import com.tiesdb.protocol.v0r0.impl.ebml.handler.ChequeHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.handler.EntryHeaderHandler;
import com.tiesdb.protocol.v0r0.test.util.StreamPacketInput;
import com.tiesdb.protocol.v0r0.test.util.StreamPacketOutput;

@DisplayName("TiesDBProtocol Miscelaneous Test")
public class TiesDBProtocolTest {

	@Test
	@DisplayName("Protocol Service Loading")
	void testProtocolServiceLoading() {
		List<TiesDBProtocol> protocols = TiesDBProtocolManager.loadProtocols();
		assertFalse(protocols.isEmpty(), "No Protocols found");
		assertEquals(1, protocols.size());
		assertEquals(new TiesDBProtocolV0R0().getVersion(), protocols.get(0).getVersion());
	}

	// TODO!!! write protocol message send test

	@Test
	@DisplayName("Protocol Message Sending And Recieving Test")
	void testProtocolMessageSendingAndRecievingTest() {
		List<TiesDBProtocol> protocols = TiesDBProtocolManager.loadProtocols();
		assertFalse(protocols.isEmpty(), "No Protocols found");
		assertEquals(1, protocols.size());
		TiesDBProtocol protocol = protocols.get(0);

		ECKey key = new ECKey();

		long timestamp = System.currentTimeMillis();

		DataEntryHeader dataEntryHeader = new DataEntryHeader();
		dataEntryHeader.setEntryType(DataEntryType.NEW);
		dataEntryHeader.setEntryVersion(1);
		dataEntryHeader.setTableName("testTable");
		dataEntryHeader.setTablespaceName("testTablespace");
		dataEntryHeader.setTimestamp(timestamp);
		dataEntryHeader.setFieldsHash(new byte[] { (byte) 0xff });

		FieldValue dataEntryFieldValue = new FieldValue();
		dataEntryFieldValue.setData("testFieldValue".getBytes());
		dataEntryFieldValue.setType("STRING");

		DataEntryField dataEntryField = new DataEntryField();
		dataEntryField.setFieldHash(new byte[] { (byte) 0xff });
		dataEntryField.setName("testFieldName");
		dataEntryField.setValue(dataEntryFieldValue);

		ChequeData chequeData = new ChequeData();
		chequeData.setAmount(BigInteger.valueOf(123456L));
		chequeData.setNumber(123L);
		chequeData.setRange(UUID.nameUUIDFromBytes("test".getBytes()));
		chequeData.setReceiptNodes(new BlockchainAddress[] { new EthereumAddress("abcdefghijklmnopqrst".getBytes()) });
		chequeData.setTimestamp(timestamp);

		Cheque cheque = new Cheque();
		cheque.setChequeData(chequeData);
		cheque.setChequeSigner(new EthereumAddress(key.getAddress()));

		DataEntry dataEntry = new DataEntry();
		dataEntry.setHeader(dataEntryHeader);
		dataEntry.setFields(new DataEntryField[] { dataEntryField });
		dataEntry.setCheques(new Cheque[] { cheque });
		dataEntry.setEntrySigner(new EthereumAddress(key.getAddress()));

		DataModificationRequest request = new DataModificationRequest();
		request.setConsistencyLevel(RequestConsistencyLevel.QUORUM);
		request.setDataEntry(dataEntry);

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

			protocol.createChannel(new TiesDBProtocolPacketChannel() {

				StreamPacketOutput streamPacketOutput = new StreamPacketOutput(baos);

				@Override
				public PacketInput getInput() {
					throw new IllegalStateException("Should not be called");
				}

				@Override
				public PacketOutput getOutput() {
					return streamPacketOutput;
				}

			}, new TiesDBProtocolV0R0Handler() {
				@Override
				public void handle(TiesDBConversationV0R0 conv) throws TiesDBProtocolException {
					conv.send(request, key);
				}
			});

			AtomicReference<Request> recievedRequestContainer = new AtomicReference<>();

			protocol.acceptChannel(new TiesDBProtocolPacketChannel() {
				StreamPacketInput streamPacketInput = new StreamPacketInput(new ByteArrayInputStream(baos.toByteArray()));

				@Override
				public PacketInput getInput() {
					return streamPacketInput;
				}

				@Override
				public PacketOutput getOutput() {
					throw new IllegalStateException("Should not be called");
				}

			}, new TiesDBProtocolV0R0Handler() {
				@Override
				public void handle(TiesDBConversationV0R0 conv) throws TiesDBProtocolException {
					Request recievedRequest = conv.recieve();

					assertTrue(Objects.deepEquals(request, recievedRequest));

					recievedRequestContainer.compareAndSet(null, recievedRequest);

					if (recievedRequest instanceof DataModificationRequest) {
						DataModificationRequest recieved = (DataModificationRequest) recievedRequest;
						DataEntryHeader header = recieved.getDataEntry().getHeader();
						try {
							TiesDBEBMLParser parser = new TiesDBEBMLParser(
									new StreamPacketInput(new ByteArrayInputStream(header.getHeaderRawBytes())),
									TiesDBEBMLTypeContainer.DATA_ENTRY.getContext());

							DataEntryHeader headerCheck = EntryHeaderHandler.INSTANCE.read(parser);
							assertTrue(Objects.deepEquals(header, headerCheck));
						} catch (IOException e) {
							fail(e);
						}

						Cheque cheque = recieved.getDataEntry().getCheques()[0];
						assertEquals(cheque.getChequeSigner(), new EthereumAddress(key.getAddress()));
						try {
							TiesDBEBMLParser parser = new TiesDBEBMLParser(
									new StreamPacketInput(new ByteArrayInputStream(cheque.getChequeRawBytes())),
									TiesDBEBMLTypeContainer.ROOT_CTX);
							Cheque chequeCheck = ChequeHandler.INSTANCE.read(parser);
							assertTrue(Objects.deepEquals(cheque, chequeCheck));
						} catch (IOException e) {
							fail(e);
						}

					}
				}
			});
			assertNotNull(recievedRequestContainer.get());
			try (ByteArrayOutputStream baosCheck = new ByteArrayOutputStream()) {
				protocol.createChannel(new TiesDBProtocolPacketChannel() {

					StreamPacketOutput streamPacketOutput = new StreamPacketOutput(baosCheck);

					@Override
					public PacketInput getInput() {
						throw new IllegalStateException("Should not be called");
					}

					@Override
					public PacketOutput getOutput() {
						return streamPacketOutput;
					}

				}, new TiesDBProtocolV0R0Handler() {
					@Override
					public void handle(TiesDBConversationV0R0 conv) throws TiesDBProtocolException {
						conv.send(recievedRequestContainer.get(), key);
						assertArrayEquals(baos.toByteArray(), baosCheck.toByteArray());
					}
				});
			}
		} catch (IOException | TiesDBProtocolException e) {
			e.printStackTrace();
		}
		assertEquals(new TiesDBProtocolV0R0().getVersion(), protocols.get(0).getVersion());
	}
}
