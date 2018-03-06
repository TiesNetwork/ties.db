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
package network.tiesdb.exception.util;

import network.tiesdb.context.api.annotation.util.TiesConfigElementHelper;

/**
 * Helper class to manage TiesDB messages.
 * 
 * <P>Contains utility methods for TiesDB message generation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public final class MessageHelper {

	public static String notFullyCompatible(Class<?> c1, Class<?> c2) {
		String binding1 = TiesConfigElementHelper.getFor(c1);
		String binding2 = TiesConfigElementHelper.getFor(c2);
		return new StringBuilder()//
				.append(null != binding1 ? binding1 : c1.getName())//
				.append(" is not fully compatible with ")//
				.append(null != binding2 ? binding2 : c2.getName())//
				.toString();
	}

}
