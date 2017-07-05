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
package network.tiesdb.context.impl.yaml;

import java.beans.IntrospectionException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.MissingProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import network.tiesdb.context.api.TiesContext;
import network.tiesdb.context.api.TiesContextFactory;
import network.tiesdb.exception.TiesConfigurationException;

/**
 * TiesDB YAML context factory class.
 * 
 * <P>Reads configuration from YAML file for context creation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class YAMLContextFactory implements TiesContextFactory {

	private static final String CONTEXT_TYPE_NAME = "yaml";

	@Override
	public TiesContext readContext(InputStream is) throws TiesConfigurationException {
		PropertiesChecker propertiesChecker = new PropertiesChecker();
		Constructor constructor = new CustomConstructor(TiesContext.class);
		constructor.setPropertyUtils(propertiesChecker);
		Yaml yaml = new Yaml(constructor);
		TiesContext context = yaml.loadAs(is, TiesContext.class);
		propertiesChecker.check();
		return context;
	}

	static class CustomConstructor extends Constructor {
		CustomConstructor(Class<?> theRoot) {
			super(theRoot);
		}

		@Override
		protected List<Object> createDefaultList(int initSize) {
			return Lists.newCopyOnWriteArrayList();
		}

		@Override
		protected Map<Object, Object> createDefaultMap() {
			return Maps.newConcurrentMap();
		}

		@Override
		protected Set<Object> createDefaultSet(int initSize) {
			return Sets.newConcurrentHashSet();
		}

		@Override
		protected Set<Object> createDefaultSet() {
			return Sets.newConcurrentHashSet();
		}
	}

	private static class PropertiesChecker extends PropertyUtils {
		private final Set<String> prohibitedProperties = new HashSet<>();

		private final Set<String> invalidProperties = new HashSet<>();

		public PropertiesChecker() {
			setSkipMissingProperties(true);
		}

		@Override
		public Property getProperty(Class<? extends Object> type, String name) throws IntrospectionException {
			final Property result = super.getProperty(type, name);

			if (result instanceof MissingProperty) {
				prohibitedProperties.add(result.getName());
			}

			return new Property(result.getName(), result.getType()) {
				@Override
				public void set(Object object, Object value) throws Exception {
					if (value == null && get(object) != null) {
						invalidProperties.add(getName());
					}
					result.set(object, value);
				}

				@Override
				public Class<?>[] getActualTypeArguments() {
					return result.getActualTypeArguments();
				}

				@Override
				public Object get(Object object) {
					return result.get(object);
				}
			};
		}

		public void check() throws TiesConfigurationException {
			if (!invalidProperties.isEmpty()) {
				throw new TiesConfigurationException(
						"Invalid yaml. Those properties " + invalidProperties + " are not valid");
			}

			if (!prohibitedProperties.isEmpty()) {
				throw new TiesConfigurationException("Invalid yaml. Please remove properties " + prohibitedProperties
						+ " from your configuration file");
			}
		}
	}

	@Override
	public void writeContext(OutputStream os, TiesContext ctx) throws TiesConfigurationException {
		PrintWriter pw = new PrintWriter(os);
		pw.print(new Yaml().dumpAsMap(ctx));
		pw.flush();
	}

	@Override
	public boolean matchesContextType(String contextTypeName) {
		return CONTEXT_TYPE_NAME.equals(contextTypeName) || getClass().getName().equals(contextTypeName);
	}

}