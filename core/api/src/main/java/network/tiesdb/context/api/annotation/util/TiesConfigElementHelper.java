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
package network.tiesdb.context.api.annotation.util;

import network.tiesdb.context.api.annotation.TiesConfigElement;

/**
 * Helper class to manage TiesDB configuration elements.
 * 
 * <P>
 * Contains utility methods for TiesDB configuration elements handling.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public final class TiesConfigElementHelper {

    private static final String[] NO_BINDINGS = new String[] {};

    public static String[] getAllFor(Class<?> c, int limit) {
        if (null == c || limit <= 0) {
            return NO_BINDINGS;
        }
        TiesConfigElement annotation = c.getDeclaredAnnotation(TiesConfigElement.class);
        if (null == annotation) {
            return NO_BINDINGS;
        }
        return annotation.value();
    }

    public static String getFor(Class<?> c) {
        String[] bindings = getAllFor(c, 1);
        return bindings.length > 0 ? bindings[0] : null;
    }

    public static String[] getAllFor(Class<?> c) {
        return getAllFor(c, Integer.MAX_VALUE);
    }

    public static String getFor(Object o) {
        return getFor(o.getClass());
    }

    public static String[] getAllFor(Object o) {
        return getAllFor(o.getClass());
    }

}