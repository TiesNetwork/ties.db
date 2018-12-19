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
 * Exception class for TiesDB system.
 * 
 * <P>
 * Parent class for any checked exceptions regarding TiesDB logic.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesException extends Exception {

    private static final long serialVersionUID = -8643212298857531130L;

    public TiesException(String message, Throwable cause) {
        super(message, cause);
    }

    public TiesException(String message) {
        super(message);
    }

}
