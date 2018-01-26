package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.v0r0.api.message.DataEntryType;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBProtocolHandlerException;

import one.utopic.sparse.ebml.reader.EBMLByteReader;
import one.utopic.sparse.ebml.util.ByteArrayInput;

public class EntryTypeHandler implements TiesDBEBMLHandler<DataEntryType> {

	private static final Logger LOG = LoggerFactory.getLogger(EntryTypeHandler.class);

	public static final EntryTypeHandler INSTANCE = new EntryTypeHandler();

	private EntryTypeHandler() {
	}

	private static final Map<Byte, DataEntryType> typeByCode;
	static {
		DataEntryType[] values = DataEntryType.values();
		HashMap<Byte, DataEntryType> map = new HashMap<>();
		for (int i = 0; i < values.length; i++) {
			try {
				byte code = getCode(values[i]);
				if (null != map.put(code, values[i])) {
					throw new InstantiationError("DataEntryType duplicate code " + code + " for " + values[i]);
				}
			} catch (TiesDBProtocolHandlerException e) {
				LOG.error("DataEntryType failed for {}", values[i], e);
				throw new InstantiationError(e.getMessage());
			}
		}
		typeByCode = Collections.unmodifiableMap(map);
	}

	private static DataEntryType getByCode(byte b) {
		return typeByCode.get(b);
	}

	private static byte getCode(DataEntryType dataEntryType) throws TiesDBProtocolHandlerException {
		switch (dataEntryType) {
		case DELETED:
			return -1;
		case MODIFICATION:
			return 1;
		case NEW:
			return Byte.MAX_VALUE;
		}
		throw new TiesDBProtocolHandlerException("Unknown DataEntryType " + dataEntryType);
	}

	@Override
	public DataEntryType read(TiesDBEBMLParser parser) throws IOException, TiesDBProtocolHandlerException {
		byte[] levelCode = new EBMLByteReader().read(parser);
		if (levelCode.length != 1) {
			LOG.debug("DataEntryType wrong code: {}", Arrays.asList(levelCode));
			throw new TiesDBProtocolHandlerException("DataEntryType code size missmatch");
		}
		return getByCode(levelCode[0]);
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(DataEntryType o) throws IOException {
		return new Part<TiesDBEBMLFormatter>() {

			@Override
			public void write(TiesDBEBMLFormatter formatter) throws IOException {
				formatter.write(new ByteArrayInput(new byte[] { getCode(o) }));
			}

			@Override
			public int getSize(TiesDBEBMLFormatter formatter) throws IOException {
				return 1;
			}

		};
	}

}
