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
package com.tiesdb.protocol;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import com.tiesdb.protocol.api.TiesDBProtocol;

public final class TiesDBProtocolManager {

	public static List<TiesDBProtocol> loadProtocols() {
		return loadProtocols(Thread.currentThread().getContextClassLoader());
	}

	public static List<TiesDBProtocol> loadProtocols(ClassLoader cl) {
		return loadProtocolsTo(new LinkedList<>(), cl);
	}

	public static <C extends Collection<? super TiesDBProtocol>> C loadProtocolsTo(C protocols) {
		return loadProtocolsTo(protocols, Thread.currentThread().getContextClassLoader());
	}

	public static <C extends Collection<? super TiesDBProtocol>> C loadProtocolsTo(C protocols, ClassLoader cl) {
		ServiceLoader.load(TiesDBProtocol.class, cl).forEach(protocol -> protocols.add(protocol));
		return protocols;
	}
}
