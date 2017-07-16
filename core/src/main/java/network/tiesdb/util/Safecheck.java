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
package network.tiesdb.util;

/**
 * TiesDB utility checks.
 * 
 * <P>Defines utilisy functions for basic sanity checks.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public final class Safecheck {

	public static <T> T nullsafe(T input) {
		return nullsafe(input, "The input should not be null");
	}

	public static <T> T nullsafe(T input, String message) {
		if (null == input) {
			throw new NullPointerException(message);
		}
		return input;
	}

	public static <T, R extends T> T nullreplace(T input, R replace) {
		return null != input ? input : replace;
	}
}
