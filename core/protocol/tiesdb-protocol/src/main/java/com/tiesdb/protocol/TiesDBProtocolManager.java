package com.tiesdb.protocol;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

public final class TiesDBProtocolManager {

	public static List<TiesDBProtocol> loadProtocols() {
		return loadProtocols(Thread.currentThread().getContextClassLoader());
	}

	public static List<TiesDBProtocol> loadProtocols(ClassLoader cl) {
		return loadProtocols(new LinkedList<>(), cl);
	}

	public static <C extends Collection<? super TiesDBProtocol>> C loadProtocols(C protocols) {
		return loadProtocols(protocols, Thread.currentThread().getContextClassLoader());
	}

	public static <C extends Collection<? super TiesDBProtocol>> C loadProtocols(C protocols, ClassLoader cl) {
		ServiceLoader.load(TiesDBProtocol.class, cl).forEach(protocol -> protocols.add(protocol));
		return protocols;
	}
}
