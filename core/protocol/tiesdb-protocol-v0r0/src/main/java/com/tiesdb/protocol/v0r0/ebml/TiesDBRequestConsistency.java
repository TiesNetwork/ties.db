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
package com.tiesdb.protocol.v0r0.ebml;

import java.util.Objects;

public class TiesDBRequestConsistency {

    public static enum ConsistencyType {
        QUORUM, PERCENT, COUNT
    }

    private final ConsistencyType type;
    private final int value;

    public TiesDBRequestConsistency(ConsistencyType type, Integer value) {
        if (ConsistencyType.QUORUM.equals(type) && 0 != value) {
            throw new IllegalArgumentException(ConsistencyType.QUORUM + " type rawValue should be 0");
        }
        this.type = Objects.requireNonNull(type);
        this.value = Objects.requireNonNull(value);
    }

    public ConsistencyType getType() {
        return type;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "TiesDBRequestConsistency [type=" + type + ", rawValue=" + value + "]";
    }

}
