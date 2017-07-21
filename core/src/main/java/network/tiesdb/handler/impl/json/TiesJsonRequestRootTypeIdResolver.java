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
package network.tiesdb.handler.impl.json;

import static network.tiesdb.util.Safecheck.nullreplace;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.type.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.context.api.annotation.util.AnnotationHelper;

/**
 * TiesDB JSON request type resolver.
 * 
 * <P>Resolves types defined in JSON request to TiesDB native types.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesJsonRequestRootTypeIdResolver implements TypeIdResolver {

	private static final Logger logger = LoggerFactory.getLogger(TiesJsonRequestRootTypeIdResolver.class);

	private static final Map<String, Class<?>> CLASS_BINDINGS;
	static {
		logger.trace("Initializing TiesJsonRequestRoot TypeIdResolver static class bindings...");
		Map<String, Class<?>> bindingMap = new HashMap<>();
		try {
			AnnotationHelper h = new AnnotationHelper();
			Collection<Class<? extends Object>> bindings = h.getBindings(TiesRequestRoot.class);
			if (null != bindings && !bindings.isEmpty()) {
				for (Class<? extends Object> binding : bindings) {
					logger.trace("Found baseType: {}", binding);
					TiesRequestRoot tiesRoot = binding.getAnnotation(TiesRequestRoot.class);
					String name = nullreplace(tiesRoot.value(), binding.getClass().getSimpleName());
					Object prev = bindingMap.put(name, binding);
					if (null != prev) {
						logger.warn("Replaced baseType {}: {} baseType was replaced by {}", name, prev, binding);
					}
				}
			}
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
		CLASS_BINDINGS = Collections.unmodifiableMap(bindingMap);
		logger.trace("TiesJsonRequestRoot TypeIdResolver static class bindings initialized successfully");
	}

	private JavaType baseType = null;

	@Override
	public String idFromValue(Object value) {
		TiesRequestRoot requestRoot = value.getClass().getAnnotation(TiesRequestRoot.class);
		return null == requestRoot ? null : requestRoot.value();
	}

	@Override
	public String idFromValueAndType(Object value, Class<?> suggestedType) {
		TiesRequestRoot requestRoot = suggestedType.getAnnotation(TiesRequestRoot.class);
		return null == requestRoot ? idFromValue(value) : requestRoot.value();
	}

	@Override
	public JavaType typeFromId(String id) {
		return getBinding(id);
	}

	private JavaType getBinding(String id) {
		Class<?> binding = null == id ? null : CLASS_BINDINGS.get(id);
		return null == binding ? null : baseType.narrowBy(binding);
	}

	@Override
	public Id getMechanism() {
		return Id.CUSTOM;
	}

	@Override
	public void init(JavaType baseType) {
		this.baseType = baseType;
	}

}
