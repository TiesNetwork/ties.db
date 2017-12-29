/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project.If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.protocol.v0.test.util;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.util.Iterator;

import com.tiesdb.protocol.api.TiesDBProtocolPacketChannel;
import com.tiesdb.protocol.api.data.Element;
import com.tiesdb.protocol.api.data.ElementContainer;

public class TestHelper {

	public static void fakeInput(String hexString, TiesDBProtocolPacketChannel channel) {
		when(channel.getInput()).thenReturn(new HexStringInput(hexString));
	}

	public static void fakeOutput(OutputStream out, TiesDBProtocolPacketChannel channel) {
		when(channel.getOutput()).thenReturn(new HexStringOutput(out));
	}

	public static void assertDeepEquals(Element e1, Element e2) {
		if (e1 instanceof ElementContainer<?> && e2 instanceof ElementContainer<?>) {
			Iterator<?> i1 = ((ElementContainer<?>) e1).iterator();
			Iterator<?> i2 = ((ElementContainer<?>) e2).iterator();
			while (i1.hasNext() && i2.hasNext()) {
				assertDeepEquals((Element) i1.next(), (Element) i2.next());
			}
			assertFalse("Size missmatch", i1.hasNext() || i2.hasNext());
		} else {
			assertEquals(e1, e2);
		}
	}

	public static void printElementTree(Element cnt) {
		printElementTree(cnt, 0);
	}

	public static void printElementTree(Element elm, int level) {
		System.out.println(getPadding(level) + elm.getType() + "@" + Integer.toHexString(elm.hashCode()));
		if (elm instanceof ElementContainer<?>) {
			for (Element e : (ElementContainer<?>) elm) {
				printElementTree(e, level + 1);
			}
		}
	}

	private static String getPadding(int level) {
		if (level == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < level; i++) {
			sb.append("    ");
		}
		return sb.toString();
	}
}
