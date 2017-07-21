/*
 * Copyright 2017 Ties BV
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package network.tiesdb.handler.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.api.TiesVersion;
import network.tiesdb.api.TiesVersion.ToString;
import network.tiesdb.context.api.TiesHandlerConfig;
import network.tiesdb.exception.TiesException;
import network.tiesdb.handler.api.TiesHandler;
import network.tiesdb.handler.impl.json.TiesJsonObjectMapperModule;
import network.tiesdb.handler.impl.json.TiesJsonRequestRoot;
import network.tiesdb.service.impl.TiesServiceImpl;
import network.tiesdb.transport.api.TiesRequest;
import network.tiesdb.transport.api.TiesResponse;
import network.tiesdb.transport.api.TiesTransport;

/**
 * TiesDB handler implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesHandlerImpl implements TiesHandler {

	private static final TiesHandlerImplVersion IMPLEMENTATION_VERSION = TiesHandlerImplVersion.v_0_0_1_prealpha;

	private static final Logger logger = LoggerFactory.getLogger(TiesHandlerImpl.class);

	private final TiesServiceImpl service;

	private final TiesHandlerConfigImpl config;

	public TiesHandlerImpl(TiesServiceImpl service, TiesHandlerConfigImpl config) {
		if (null == config) {
			throw new NullPointerException("The config should not be null");
		}
		if (null == service) {
			throw new NullPointerException("The service should not be null");
		}
		this.service = service;
		this.config = config;
	};

	@Override
	public void handle(final TiesRequest request, final TiesResponse response) throws TiesException {
		logger.trace("Call to network.tiesdb.handler.impl.TiesHandlerImpl.handle(request, response)");
		try {
			handleInternal1(request, response);
		} catch (IOException e) {
			throw new TiesException("Can't process request", e);
		}
	}

	protected void handleInternal1(TiesRequest request, TiesResponse response)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = createConfiguredMapper();
		TiesJsonRequestRoot jsonRequest = mapper.readValue(request.getInputStream(), TiesJsonRequestRoot.class);
		mapper.writerWithDefaultPrettyPrinter().writeValue(response.getOutputStream(), jsonRequest);
	}

	private ObjectMapper createConfiguredMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		mapper.registerModule(new TiesJsonObjectMapperModule());
		return mapper;
	}

	protected void handleInternal(TiesRequest request, TiesResponse response) {
		ObjectMapper mapper = createConfiguredMapper();
		try (InputStream is = request.getInputStream()) {
			@SuppressWarnings("unchecked")
			Map<String, Object> jsonMap = mapper.readValue(is, Map.class);
			Iterator<Entry<String, Object>> iter = jsonMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Object> entry = iter.next();
				iter.remove();
				jsonMap.put(entry.getKey().toLowerCase(), entry.getValue().toString().toUpperCase());
			}

			jsonMap.put("serviceVersion", ToString.format(service.getVersion()));

			List<Map<?, ?>> transportsVersions = new ArrayList<>();
			for (TiesTransport t : service.getTransports()) {
				HashMap<Object, Object> map = new HashMap<>();
				map.put("transportVersion", ToString.format(t.getVersion()));
				map.put("handlerVersion", ToString.format(t.getHandler().getVersion()));
				transportsVersions.add(map);
			}
			jsonMap.put("transportsVersions", transportsVersions);
			try (OutputStream os = response.getOutputStream()) {
				os.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap)
						.getBytes(config.getCharset()));
			}
			System.out.println(jsonMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public TiesVersion getVersion() {
		return IMPLEMENTATION_VERSION;
	}

	@Override
	public TiesHandlerConfig getTiesHandlerConfig() {
		return config;
	}

}
