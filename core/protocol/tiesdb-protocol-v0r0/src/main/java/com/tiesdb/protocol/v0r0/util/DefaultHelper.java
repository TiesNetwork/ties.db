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
package com.tiesdb.protocol.v0r0.util;

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
