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
package network.tiesdb.service.impl.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.api.TiesHandler;
import network.tiesdb.api.TiesRequest;
import network.tiesdb.api.TiesResponse;
import network.tiesdb.service.impl.TiesServiceImpl;

/**
 * TiesDB handler implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesHandlerImpl implements TiesHandler {

	private static final Logger logger = LoggerFactory.getLogger(TiesHandlerImpl.class);

	private final TiesServiceImpl service;

	private TiesHandlerConfigImpl config;

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
	public void handle(final TiesRequest request, final TiesResponse response) {
		logger.trace("Call to network.tiesdb.service.impl.handler.TiesHandlerImpl.handle(request, response)");
		handleInternal(request, response);
	}

	protected void handleInternal(TiesRequest request, TiesResponse response) {
		ObjectMapper mapper = new ObjectMapper();
		try (InputStream is = request.getInputStream()) {
			@SuppressWarnings("unchecked")
			Map<String, Object> jsonMap = mapper.readValue(is, Map.class);
			Iterator<Entry<String, Object>> iter = jsonMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Object> entry = iter.next();
				iter.remove();
				jsonMap.put(entry.getKey().toLowerCase(), entry.getValue().toString().toUpperCase());
			}
			jsonMap.put("apiVersion", service.getApiVersion());
			jsonMap.put("implVersion", service.getImplVersion());
			try (OutputStream os = response.getOutputStream()) {
				os.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap).getBytes(config.getCharset()));
			}
			System.out.println(jsonMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
