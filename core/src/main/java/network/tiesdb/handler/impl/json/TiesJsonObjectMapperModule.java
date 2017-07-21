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

import java.lang.reflect.Type;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.AbstractTypeResolver;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.map.type.TypeBindings;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.type.TypeModifier;
import org.codehaus.jackson.type.JavaType;

/**
 * Jackson mapper for TiesDB JSON requests.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
//FIXME remove
@Deprecated
public class TiesJsonObjectMapperModule extends Module {

	private static final Version VERSION = new Version(0, 0, 1, "prealpha");

	@Override
	public String getModuleName() {
		return "TiesJson";
	}

	@Override
	public Version version() {
		return VERSION;
	}

	@Override
	public void setupModule(SetupContext context) {
		context.addTypeModifier(TiesJsonTypeModifier.getInstance());
		context.addAbstractTypeResolver(TiesJsonRequestAbstractTypeResolver.getInstance());
	}

	public static class TiesJsonTypeModifier extends TypeModifier {

		private static final TiesJsonTypeModifier INSTANCE = new TiesJsonTypeModifier();

		public static TiesJsonTypeModifier getInstance() {
			return INSTANCE;
		}

		@Override
		public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
			return type;
		}

	}

	public static class TiesJsonRequestAbstractTypeResolver extends AbstractTypeResolver {

		private static final TiesJsonRequestAbstractTypeResolver INSTANCE = new TiesJsonRequestAbstractTypeResolver();

		public static TiesJsonRequestAbstractTypeResolver getInstance() {
			return INSTANCE;
		}

		@Override
		public JavaType resolveAbstractType(DeserializationConfig config, JavaType type) {
			return super.resolveAbstractType(config, type);
		}

	}
}
