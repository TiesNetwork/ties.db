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
 * Exception class for TiesDB initialization logic.
 * 
 * <P>
 * Exception thrown during startup process.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesStartupException extends TiesException {

    private static final long serialVersionUID = -2119062737416144376L;

    private final Integer exitCode;

    public TiesStartupException(Integer exitCode, String message, Throwable cause) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public TiesStartupException(Integer exitCode, String message) {
        super(message);
        this.exitCode = exitCode;
    }

    public Integer getExitCode() {
        return exitCode;
    }

}
