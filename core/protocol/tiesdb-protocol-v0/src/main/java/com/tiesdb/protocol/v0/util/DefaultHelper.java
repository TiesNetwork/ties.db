package com.tiesdb.protocol.v0.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DefaultHelper {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultHelper.class);

	public static boolean traceFull;

	public static final void trace(String message, Object... obj) {
		LOG.debug(message, obj);
		Exception ex = new Exception();
		StackTraceElement[] st = ex.getStackTrace();
		int len = traceFull ? st.length : 3;
		for (int i = 1; i < len; i++) {
			LOG.debug("\tat {}", st[i]);
		}
	}

	private DefaultHelper() {
	}
}
