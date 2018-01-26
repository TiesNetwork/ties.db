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

import com.tiesdb.protocol.v0r0.api.message.Cheque;
import com.tiesdb.protocol.v0r0.api.message.DataEntry;
import com.tiesdb.protocol.v0r0.api.message.DataEntryField;
import com.tiesdb.protocol.v0r0.api.message.DataModificationRequest;
import com.tiesdb.protocol.v0r0.api.message.FieldValue;
import com.tiesdb.protocol.v0r0.api.message.DataEntryHeader;
import com.tiesdb.protocol.v0r0.api.message.DataEntryType;
import com.tiesdb.protocol.v0r0.api.message.RequestConsistencyLevel;
import com.tiesdb.protocol.v0r0.api.message.part.BlockchainAddress;
import com.tiesdb.protocol.v0r0.impl.EthereumAddress;
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
			data.setFields(new DataEntryField[] { new DataEntryField() });
			data.setCheques(new Cheque[] { new Cheque() });
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
		});
	}

	@Test
	@DisplayName("Cheque")
	void testCheque() throws IOException {
		testReadWrite(new Cheque(), ChequeHandler.INSTANCE, TiesDBEBMLTypeContainer.CHEQUE, data -> {
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
