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
package network.tiesdb.util;

/**
 * TiesDB utility checks.
 * 
 * <P>
 * Defines utilisy functions for basic sanity checks.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public final class Safecheck {

    public static <T> T nullsafe(T input) {
        return nullsafe(input, "The input should not be null");
    }

    public static <T> T nullsafe(T input, String message) {
        if (null == input) {
            throw new NullPointerException(message);
        }
        return input;
    }

    public static <T, R extends T> T nullreplace(T input, R replace) {
        return null != input ? input : replace;
    }
}
