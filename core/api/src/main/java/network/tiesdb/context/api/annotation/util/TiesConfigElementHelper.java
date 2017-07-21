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
package network.tiesdb.context.api.annotation.util;

import network.tiesdb.context.api.annotation.TiesConfigElement;

/**
 * Helper class to manage TiesDB configuration elements.
 * 
 * <P>Contains utility methods for TiesDB configuration elements handling.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public final class TiesConfigElementHelper {

	private static final String[] NO_BINDINGS = new String[] {};

	public static String[] getAllFor(Class<?> c, int limit) {
		if (null == c || limit <= 0) {
			return NO_BINDINGS;
		}
		TiesConfigElement annotation = c.getDeclaredAnnotation(TiesConfigElement.class);
		if (null == annotation) {
			return NO_BINDINGS;
		}
		return annotation.value();
	}

	public static String getFor(Class<?> c) {
		String[] bindings = getAllFor(c, 1);
		return bindings.length > 0 ? bindings[0] : null;
	}

	public static String[] getAllFor(Class<?> c) {
		return getAllFor(c, Integer.MAX_VALUE);
	}

	public static String getFor(Object o) {
		return getFor(o.getClass());
	}

	public static String[] getAllFor(Object o) {
		return getAllFor(o.getClass());
	}

}