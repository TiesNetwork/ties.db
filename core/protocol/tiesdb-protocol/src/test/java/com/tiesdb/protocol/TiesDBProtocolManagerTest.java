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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tiesdb.protocol.api.TiesDBProtocol;
import com.tiesdb.protocol.api.TiesDBProtocolHandlerProvider;
import com.tiesdb.protocol.api.Version;
import com.tiesdb.protocol.exception.TiesDBProtocolException;

@DisplayName("TiesDBProtocol Manager Test")
public class TiesDBProtocolManagerTest {

    private static final Version VERSION1 = new Version(0, 0, 1);
    private static final Version VERSION2 = new Version(0, 0, 2);
    private static final Version VERSION3 = new Version(0, 0, 3);

    public static class ProtocolV1 extends Protocol {
        @Override
        public Version getVersion() {
            return VERSION1;
        }
    }

    public static class ProtocolV2 extends Protocol {
        @Override
        public Version getVersion() {
            return VERSION2;
        }
    }

    public static class ProtocolV3 extends Protocol {
        @Override
        public Version getVersion() {
            return VERSION3;
        }
    }

    @Test
    @DisplayName("Protocol Lookup Test")
    void testProtocolLookup() {
        Set<Version> versions = TiesDBProtocolManager.getProtocolVersions();
        assertTrue(versions.containsAll(Arrays.asList(VERSION1, VERSION2, VERSION3)));
        List<Object> classes = Arrays.asList(TiesDBProtocolManager.getProtocols().stream().map(p -> p.getClass()).toArray());
        assertTrue(classes.containsAll(Arrays.asList(ProtocolV1.class, ProtocolV2.class, ProtocolV3.class)));
    }

    @Test
    @DisplayName("Protocol Reload Test")
    void testProtocolReload() {
        // Sanity check
        testProtocolLookup();

        Collection<TiesDBProtocol> protocols = new ArrayList<>(TiesDBProtocolManager.getProtocols());
        assertTrue(TiesDBProtocolManager.getProtocols().containsAll(protocols));

        TiesDBProtocolManager.reloadProtocols(getClass().getClassLoader());

        assertTrue(protocols.retainAll(TiesDBProtocolManager.getProtocols()));
        assertTrue(protocols.isEmpty());

        // Sanity check
        testProtocolLookup();
    }

    @Test
    @DisplayName("Protocol Concurrency Test")
    void testProtocolConcurrency() throws InterruptedException {

        AtomicReference<Throwable> exception = new AtomicReference<>();

        abstract class Delayed implements Runnable {

            private final int delay;

            Delayed(int delay) {
                this.delay = delay;
            }

            @Override
            public void run() {
                try {
                    Collection<TiesDBProtocol> protocols = TiesDBProtocolManager.getProtocols();
                    for (TiesDBProtocol tiesDBProtocol : protocols) {
                        Thread.sleep(delay * 10);
                        assertTrue(getProtocols().contains(tiesDBProtocol));
                    }
                } catch (Throwable e) {
                    exception.compareAndSet(null, e);
                }
            }

            abstract Collection<TiesDBProtocol> getProtocols();

        }

        // --------------------
        Collection<TiesDBProtocol> protocols = new ArrayList<>(TiesDBProtocolManager.getProtocols());
        ArrayList<Thread> delays = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            delays.add(new Thread(new Delayed(i) {
                @Override
                Collection<TiesDBProtocol> getProtocols() {
                    return protocols;
                }
            }, "Delayed " + i));
        }
        for (Thread delayed : delays) {
            delayed.start();
        }
        // --------------------

        TiesDBProtocolManager.reloadProtocols(getClass().getClassLoader());

        // --------------------
        Collection<TiesDBProtocol> protocols2 = new ArrayList<>(TiesDBProtocolManager.getProtocols());
        ArrayList<Thread> delays2 = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            delays2.add(new Thread(new Delayed(i) {
                @Override
                Collection<TiesDBProtocol> getProtocols() {
                    return protocols2;
                }
            }, "Delayed2 " + i));
        }
        for (Thread delayed : delays2) {
            delayed.start();
        }
        // --------------------

        // --------------------
        for (Thread delayed : delays) {
            delayed.join();
        }
        for (Thread delayed : delays2) {
            delayed.join();
        }
    }

    private static abstract class Protocol implements TiesDBProtocol {

        @Override
        public void createChannel(TiesDBChannelOutput output, TiesDBProtocolHandlerProvider handlerProvider)
                throws TiesDBProtocolException {
        }

        @Override
        public void acceptChannel(TiesDBChannelInput input, TiesDBChannelOutput output, TiesDBProtocolHandlerProvider handlerProvider)
                throws TiesDBProtocolException {
        }

    }

}
