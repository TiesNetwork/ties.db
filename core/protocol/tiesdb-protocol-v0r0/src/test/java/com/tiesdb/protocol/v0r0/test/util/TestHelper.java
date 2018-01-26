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

	private static final HashSet<Method> filteredMethods = new HashSet<Method>();
	static {
		Method[] methods = Object.class.getMethods();
		for (int i = 0; i < methods.length; i++) {
			filteredMethods.add(methods[i]);
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
			if (filteredMethods.contains(methods[i])) {
				continue;
			}
			try {
				Method method = methods[i];
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
