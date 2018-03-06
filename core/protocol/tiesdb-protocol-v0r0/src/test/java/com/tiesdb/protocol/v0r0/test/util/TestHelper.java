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
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.protocol.v0r0.test.util;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;

import javax.xml.bind.DatatypeConverter;

import org.mockito.verification.VerificationMode;

public final class TestHelper {

	private static final HashSet<String> filteredMethods = new HashSet<>();
	static {
		Method[] methods = Object.class.getMethods();
		for (int i = 0; i < methods.length; i++) {
			filteredMethods.add(methods[i].getName());
		}
	}

	private TestHelper() {
	}

	public static StreamPacketInput createInput(String stringInput) {
		return createInput(DatatypeConverter.parseHexBinary(stringInput));
	}

	public static StreamPacketInput createInput(byte[] byteInput) {
		return new StreamPacketInput(new ByteArrayInputStream(byteInput));
	}

	public static void verifyAllInvocations(Object o, Class<?> type) {
		verifyAllInteractions(o, type, times(1));
	}

	public static <T> void verifyAllInteractions(Object o, Class<?> type, VerificationMode mode) {
		Method[] methods = type.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (filteredMethods.contains(method.getName())) {
				continue;
			}
			try {
				method.invoke(verify(o, mode), anyParams(method.getParameterTypes()));
			} catch (IllegalAccessException | IllegalArgumentException e) {
				throw new AssertionError(e);
			} catch (InvocationTargetException e) {
				Throwable cause = e.getCause();
				if (cause instanceof AssertionError) {
					throw (AssertionError) cause;
				}
			}
		}
	}

	private static Object[] anyParams(Class<?>[] parameterTypes) {
		Object[] parameters = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			parameters[i] = any(parameterTypes[i]);
		}
		return parameters;
	}
}
