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

import java.util.List;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonMappingException.Reference;
import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;

public class TiesJsonRequestError {
	public final boolean error = true;
	public String message;

	private TiesJsonRequestError(String message) {
		this.message = message;
	}

	private static String formatPath(List<Reference> path) {
		StringBuilder sb = new StringBuilder();
		for (Reference reference : path) {
			sb.append(reference.getFieldName());
			sb.append('.');
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	public static Object create(JsonMappingException e) {
		if (e instanceof UnrecognizedPropertyException) {
			return new TiesJsonRequestError("Unrecognized field " + formatPath(e.getPath()));
		}
		return new TiesJsonRequestError("Unexpected exception");
	}
}
