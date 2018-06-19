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

import java.util.Comparator;

public final class Version {

    public static enum VersionComprator implements Comparator<Version> {

        VERSION, //
        REVISION, //
        FULL, //

        ;

        @Override
        public int compare(Version v1, Version v2) {
            int c = 0;
            return v1 == null //
                    ? (v2 == null ? 0 : 1)
                    : v2 == null //
                            ? -1
                            : (c = Integer.compare(v1.version, v2.version)) != 0 || equals(VERSION) //
                                    ? c
                                    : (c = Integer.compare(v1.revision, v2.revision)) != 0 || equals(REVISION) //
                                            ? c
                                            : Integer.compare(v1.maintenceence, v2.maintenceence);
        }
    }

    private static final int MAX_UNSIGNED_VALUE = 1 + Short.MAX_VALUE * 2;

    private final int version;
    private final int revision;
    private final int maintenceence;

    public Version(int version) {
        this(version, 0, 0);
    }

    public Version(int version, int revision) {
        this(version, revision, 0);
    }

    public Version(int version, int revision, int maintence) {
        this.version = safeShort(version);
        this.revision = safeShort(revision);
        this.maintenceence = safeShort(maintence);
    }

    public int getVersion() {
        return version;
    }

    public int getRevision() {
        return revision;
    }

    public int getMaintence() {
        return maintenceence;
    }

    private static int safeShort(int v) {
        if (v < 0 || v > MAX_UNSIGNED_VALUE) {
            throw new IllegalArgumentException("Unsigned short should be 0 - " + MAX_UNSIGNED_VALUE);
        }
        return v;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + version;
        result = prime * result + revision;
        result = prime * result + maintenceence;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Version other = (Version) obj;
        if (version != other.version)
            return false;
        if (revision != other.revision)
            return false;
        if (maintenceence != other.maintenceence)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "V" + version + "R" + revision + (maintenceence != 0 ? "M" + maintenceence : "");
    }
}
