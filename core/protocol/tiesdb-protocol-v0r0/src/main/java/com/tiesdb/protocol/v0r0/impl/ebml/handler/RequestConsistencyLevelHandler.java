package com.tiesdb.protocol.v0r0.impl.ebml.handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.v0r0.api.message.RequestConsistencyLevel;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLFormatter;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLHandler;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBEBMLParser;
import com.tiesdb.protocol.v0r0.impl.ebml.TiesDBProtocolHandlerException;

import one.utopic.sparse.ebml.reader.EBMLByteReader;
import one.utopic.sparse.ebml.util.ByteArrayInput;

public class RequestConsistencyLevelHandler implements TiesDBEBMLHandler<RequestConsistencyLevel> {

	private static final Logger LOG = LoggerFactory.getLogger(RequestConsistencyLevelHandler.class);

	public static final RequestConsistencyLevelHandler INSTANCE = new RequestConsistencyLevelHandler();

	private RequestConsistencyLevelHandler() {
	}

	private static final Map<Byte, RequestConsistencyLevel> levelByCode;
	static {
		RequestConsistencyLevel[] values = RequestConsistencyLevel.values();
		HashMap<Byte, RequestConsistencyLevel> map = new HashMap<>();
		for (int i = 0; i < values.length; i++) {
			try {
				byte code = getCode(values[i]);
				if (null != map.put(code, values[i])) {
					throw new InstantiationError("RequestConsistencyLevel duplicate code " + code + " for " + values[i]);
				}
			} catch (TiesDBProtocolHandlerException e) {
				LOG.error("RequestConsistencyLevel failed for {}", values[i], e);
				throw new InstantiationError(e.getMessage());
			}
		}
		levelByCode = Collections.unmodifiableMap(map);
	}

	private static RequestConsistencyLevel getByCode(byte b) {
		return levelByCode.get(b);
	}

	private static byte getCode(RequestConsistencyLevel requestConsistencyLevel) throws TiesDBProtocolHandlerException {
		switch (requestConsistencyLevel) {
		case ALL:
			return -1;
		case ONE:
			return 1;
		case QUORUM:
			return Byte.MAX_VALUE;
		}
		throw new TiesDBProtocolHandlerException("Unknown RequestConsistencyLevel " + requestConsistencyLevel);
	}

	@Override
	public RequestConsistencyLevel read(TiesDBEBMLParser parser) throws IOException, TiesDBProtocolHandlerException {
		byte[] levelCode = new EBMLByteReader().read(parser);
		if (levelCode.length != 1) {
			LOG.debug("RequestConsistencyLevel wrong code: {}", Arrays.asList(levelCode));
			throw new TiesDBProtocolHandlerException("RequestConsistencyLevel code size missmatch");
		}
		return getByCode(levelCode[0]);
	}

	@Override
	public Part<TiesDBEBMLFormatter> prepare(RequestConsistencyLevel o) throws IOException {
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
