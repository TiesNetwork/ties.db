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
package com.tiesdb.protocol;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.tiesdb.protocol.api.TiesDBProtocol;
import com.tiesdb.protocol.api.Version;

public final class TiesDBProtocolManager {

    private static final Map<Version, TiesDBProtocol> PROTOCOL_MAP = new ConcurrentHashMap<>();
    private static final Collection<TiesDBProtocol> PROTOCOLS = Collections.unmodifiableCollection(PROTOCOL_MAP.values());
    private static final Set<Version> PROTOCOL_VERSIONS = Collections.unmodifiableSet(PROTOCOL_MAP.keySet());

    static {
        reloadProtocols(Thread.currentThread().getContextClassLoader());
    }

    synchronized public static void reloadProtocols(ClassLoader classLoader) {
        Iterator<TiesDBProtocol> pIter = ServiceLoader.load(TiesDBProtocol.class, classLoader).iterator();
        PROTOCOL_MAP.clear();
        while (pIter.hasNext()) {
            TiesDBProtocol p = pIter.next();
            PROTOCOL_MAP.put(p.getVersion(), p);
        }
    }

    public static Collection<TiesDBProtocol> getProtocols() {
        return PROTOCOLS;
    }

    public static Set<Version> getProtocolVersions() {
        return PROTOCOL_VERSIONS;
    }

    public static TiesDBProtocol getProtocol(Version version) {
        return PROTOCOL_MAP.get(version);
    }

}
