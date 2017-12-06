package com.tiesdb.protocol.v0.test.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.OutputStream;

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
			int size = ((ElementContainer<?>) e1).size();
			assertEquals(size, ((ElementContainer<?>) e2).size(), e1 + " " + e2 + " size missmatch");
			for (int i = 0; i < size; i++) {
				assertDeepEquals(((ElementContainer<?>) e1).get(i), ((ElementContainer<?>) e2).get(i));
			}
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
