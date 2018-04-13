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
package com.tiesdb.protocol.api;

import static com.tiesdb.protocol.api.Version.VersionComprator;

import java.util.Comparator;

import com.tiesdb.protocol.exception.TiesDBException;
import com.tiesdb.protocol.exception.TiesDBProtocolException;

import one.utopic.abio.api.Closable;
import one.utopic.abio.api.Skippable;
import one.utopic.abio.api.input.Input;
import one.utopic.abio.api.output.Flushable;
import one.utopic.abio.api.output.Output;

public interface TiesDBProtocol {

    public static enum ProtocolComparator implements Comparator<TiesDBProtocol> {

        REVISION(VersionComprator.REVISION), //
        VERSION(VersionComprator.VERSION), //
        FULL(VersionComprator.FULL), //

        ;

        private final VersionComprator versionComparatorThreshold;

        private ProtocolComparator(VersionComprator versionComparatorThreshold) {
            this.versionComparatorThreshold = versionComparatorThreshold;
        }

        @Override
        public int compare(TiesDBProtocol p1, TiesDBProtocol p2) {
            return p1 == null
                ? (p2 == null ? 0 : 1)
                : p2 == null
                    ? -1 //
                    : versionComparatorThreshold.compare(p1.getVersion(), p2.getVersion());
        }

    }

    interface TiesDBChannelInput extends Input, Skippable, Closable {
    }

    interface TiesDBChannelOutput extends Output, Flushable, Closable {
    }

    interface TiesDBProtocolProcessor<T> {

        TiesDBProtocol getProtocol();

        void processChannel(TiesDBChannelInput input, TiesDBChannelOutput output)
                throws TiesDBProtocolException;

    }

    Version getVersion();

    void createChannel(TiesDBChannelInput input, TiesDBChannelOutput output, TiesDBProtocolHandlerProvider handlerProvider) throws TiesDBException;

    void acceptChannel(TiesDBChannelInput input, TiesDBChannelOutput output, TiesDBProtocolHandlerProvider handlerProvider) throws TiesDBException;

}
