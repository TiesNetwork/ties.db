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
package network.tiesdb.api;

/**
 * TiesDB API version implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public enum TiesApiVersion implements TiesVersion {

    v_0_0_1_prealpha(0, 0, 1, "prealpha"), //
    ;

    private final Integer majorVersion;
    private final Integer minorVersion;
    private final Integer incrementalVersion;
    private final String qualifer;

    private TiesApiVersion(Integer majorVersion, Integer minorVersion, Integer incrementalVersion) {
        this(majorVersion, minorVersion, incrementalVersion, null);
    }

    private TiesApiVersion(Integer majorVersion, Integer minorVersion, Integer incrementalVersion, String qualifer) {
        if (null == majorVersion) {
            throw new NullPointerException("The majorVersion should not be null");
        }
        if (null == minorVersion) {
            throw new NullPointerException("The minorVersion should not be null");
        }
        if (null == incrementalVersion) {
            throw new NullPointerException("The incrementalVersion should not be null");
        }
        if (null != qualifer && qualifer.isEmpty()) {
            qualifer = null;
        }
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.incrementalVersion = incrementalVersion;
        this.qualifer = qualifer;
    }

    @Override
    public Integer getMajorVersion() {
        return majorVersion;
    }

    @Override
    public Integer getMinorVersion() {
        return minorVersion;
    }

    @Override
    public Integer getIncrementalVersion() {
        return incrementalVersion;
    }

    @Override
    public String getQualifer() {
        return qualifer;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + majorVersion + "." + minorVersion + "." + incrementalVersion
                + (null != qualifer ? "." + qualifer : "") + "]";
    }

}