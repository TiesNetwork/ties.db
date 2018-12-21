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
package network.tiesdb.exception.util;

import network.tiesdb.context.api.annotation.util.TiesConfigElementHelper;

/**
 * Helper class to manage TiesDB messages.
 * 
 * <P>
 * Contains utility methods for TiesDB message generation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public final class MessageHelper {

    public static String notFullyCompatible(Class<?> c1, Class<?> c2) {
        String binding1 = TiesConfigElementHelper.getFor(c1);
        String binding2 = TiesConfigElementHelper.getFor(c2);
        return new StringBuilder()//
                .append(null != binding1 ? binding1 : c1.getName())//
                .append(" is not fully compatible with ")//
                .append(null != binding2 ? binding2 : c2.getName())//
                .toString();
    }

}
