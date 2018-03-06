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
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.UUID;
import java.util.function.Consumer;

import javax.xml.bind.DatatypeConverter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tiesdb.protocol.v0r0.api.message.BlockchainAddress;
import com.tiesdb.protocol.v0r0.api.message.Cheque;
import com.tiesdb.protocol.v0r0.api.message.ChequeData;
import com.tiesdb.protocol.v0r0.api.message.DataEntry;
import com.tiesdb.protocol.v0r0.api.message.DataEntryField;
import com.tiesdb.protocol.v0r0.api.message.DataModificationRequest;
import com.tiesdb.protocol.v0r0.api.message.EthereumAddress;
import com.tiesdb.protocol.v0r0.api.message.FieldValue;
import com.tiesdb.protocol.v0r0.api.message.DataEntryHeader;
import com.tiesdb.protocol.v0r0.api.message.DataEntryType;
import com.tiesdb.protocol.v0r0.api.message.RequestConsistencyLevel;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLTypeContainer;
import com.tiesdb.protocol.v0r0.impl.ebml.handler.*;
import com.tiesdb.protocol.v0r0.test.util.StreamPacketInput;
import com.tiesdb.protocol.v0r0.test.util.StreamPacketOutput;

import one.utopic.sparse.ebml.EBMLType;
import one.utopic.sparse.ebml.EBMLWriter.Part;

import static com.tiesdb.protocol.v0r0.test.util.TestHelper.*;

@DisplayName("TiesDB EBML Read/Write Test")
public class TiesDBEBMLReadWriteTest {

	@Test
	@DisplayName("DataModificationRequest")
	void testDataModificationRequest() throws IOException {
		testReadWrite(new DataModificationRequest(), DataModificationRequestHandler.INSTANCE, TiesDBEBMLTypeContainer.MODIFICATION_REQUEST,
				data -> {
					data.setConsistencyLevel(RequestConsistencyLevel.ONE);
					data.setDataEntry(new DataEntry());
				});
	}

	@Test
	@DisplayName("RequestConsistencyLevel")
	void testRequestConsistencyLevel() throws IOException {
		testReadWrite(RequestConsistencyLevel.QUORUM, RequestConsistencyLevelHandler.INSTANCE, TiesDBEBMLTypeContainer.REQUEST_CONSISTENCY);
	}

	@Test
	@DisplayName("DataEntryType")
	void testDataEntryType() throws IOException {
		testReadWrite(DataEntryType.MODIFICATION, EntryTypeHandler.INSTANCE, TiesDBEBMLTypeContainer.ENTRY_TYPE);
	}

	@Test
	@DisplayName("DataEntry")
	void testDataEntry() throws IOException {
		testReadWrite(new DataEntry(), DataEntryHandler.INSTANCE, TiesDBEBMLTypeContainer.DATA_ENTRY, data -> {
			data.setHeader(new DataEntryHeader());
			DataEntryField dataEntryField = new DataEntryField();
			dataEntryField.setName("test");
			data.setFields(new DataEntryField[] { dataEntryField });
			data.setCheques(new Cheque[] { new Cheque() });
			data.setEntrySigner(new EthereumAddress("FakeEthereumAddress0".getBytes()));
		});
	}

	@Test
	@DisplayName("DataEntryField")
	void testEntryField() throws IOException {
		testReadWrite(new DataEntryField(), FieldHandler.INSTANCE, TiesDBEBMLTypeContainer.FIELD, data -> {
			data.setFieldHash("testHash".getBytes());
			data.setName("testField");
			data.setValue(new FieldValue());
		});
	}

	@Test
	@DisplayName("FieldValue")
	void testFieldValue() throws IOException {
		testReadWrite(new FieldValue(), FieldValueHandler.INSTANCE, TiesDBEBMLTypeContainer.FIELD_VALUE, data -> {
			data.setType("TestType");
			data.setData("TestValue".getBytes());
		});
	}

	@Test
	@DisplayName("DataEntryHeader")
	void testEntryHeader() throws IOException {
		testReadWrite(new DataEntryHeader(), EntryHeaderHandler.INSTANCE, TiesDBEBMLTypeContainer.ENTRY_HEADER, data -> {
			data.setEntryType(DataEntryType.NEW);
			data.setEntryVersion(0);
			data.setTableName("testTable");
			data.setTablespaceName("testTablespace");
			data.setTimestamp(123456789L);
			data.setFieldsHash("testHash".getBytes());
			data.getHeaderRawBytes();
			data.setHeaderRawBytes(new byte[0]);
		});
	}

	@Test
	@DisplayName("ChequeData")
	void testChequeData() throws IOException {
		testReadWrite(new ChequeData(), ChequeDataHandler.INSTANCE, TiesDBEBMLTypeContainer.CHEQUE_DATA, data -> {
			data.setAmount(BigInteger.TEN);
			data.setNumber(12345L);
			data.setRange(UUID.randomUUID());
			data.setTimestamp(123456789L);
			data.setReceiptNodes(new BlockchainAddress[] { new EthereumAddress("FakeEthereumAddress0".getBytes()) });
		});
	}

	private <T> void testReadWrite(T data, TiesDBEBMLHandler<T> handler, EBMLType dataType, Consumer<T> fillAll) throws IOException {
		testReadWrite(data, handler, dataType, false);
		T dataSpy = spy(data);
		if (fillAll != null) {
			fillAll.accept(dataSpy);
		}
		testReadWrite(dataSpy, handler, dataType, true);
	}

	private <T> void testReadWrite(T data, TiesDBEBMLHandler<T> handler, EBMLType dataType) throws IOException {
		testReadWrite(data, handler, dataType, false);
	}

	private <T> void testReadWrite(T data, TiesDBEBMLHandler<T> handler, EBMLType dataType, boolean verifyAllInvocations)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			{
				StreamPacketOutput output = new StreamPacketOutput(baos);
				TiesDBEBMLFormatter formatter = new TiesDBEBMLFormatter(output);
				Part<TiesDBEBMLFormatter> part = handler.prepare(data);
				formatter.newHeader(dataType, part.getSize(formatter));
				part.write(formatter);
				output.flush();
			}
			if (verifyAllInvocations) {
				verifyAllInvocations(data, mockingDetails(data).getMockCreationSettings().getTypeToMock());
				verifyNoMoreInteractions(data);
			}
			ByteArrayOutputStream baosTest = new ByteArrayOutputStream();
			{
				StreamPacketInput input = createInput(baos.toByteArray());
				TiesDBEBMLParser parser = new TiesDBEBMLParser(input, dataType.getContext());
				T dataRead = handler.read(parser);
				StreamPacketOutput output = new StreamPacketOutput(baosTest);
				TiesDBEBMLFormatter formatter = new TiesDBEBMLFormatter(output);
				Part<TiesDBEBMLFormatter> part = handler.prepare(dataRead);
				formatter.newHeader(dataType, part.getSize(formatter));
				part.write(formatter);
				output.flush();
			}
			assertArrayEquals(baos.toByteArray(), baosTest.toByteArray());
		} catch (Error | RuntimeException | IOException e) {
			throw new AssertionError("TestReadWrite failed\n\t(verifyAllInvocations " + verifyAllInvocations + ")\n\t\tData: " + data
					+ "\n\t\tHandler: " + handler, e);
		} finally {
			if (Boolean.getBoolean("test-verbose")) {
				System.out.println(dataType.getName() + (verifyAllInvocations ? "  Fat" : " Thin") + ": "
						+ DatatypeConverter.printHexBinary(baos.toByteArray()));
			}
		}
	}

}
