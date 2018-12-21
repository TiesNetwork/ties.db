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
package network.tiesdb.exception;

/**
 * Exception class for TiesDB configuration logic.
 * 
 * <P>
 * Exception thrown by context factories during configuration, or by contexts if
 * configuration inconsistency found.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesConfigurationException extends TiesException {

    private static final long serialVersionUID = 6742891812521575170L;

    public TiesConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TiesConfigurationException(String message) {
        super(message);
    }

}
