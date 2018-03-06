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
 * Exception class for TiesDB handler logic.
 * 
 * <P>Exception thrown during request or response handling.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesHandleException extends TiesException {

	private static final long serialVersionUID = 7161646256137569137L;

	public TiesHandleException(String message, Throwable cause) {
		super(message, cause);
	}

	public TiesHandleException(String message) {
		super(message);
	}

}
