/*
 * Copyright 2017 Ties BV
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package network.tiesdb.exception;

/**
 * Exception class for TiesDB configuration logic.
 * 
 * <P>Exception thrown by context factories during configuration, or by contexts 
 * if configuration inconsistency found.
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
