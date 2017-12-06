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
