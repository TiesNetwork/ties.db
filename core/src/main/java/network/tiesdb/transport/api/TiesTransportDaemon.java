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
package network.tiesdb.transport.api;

import network.tiesdb.api.TiesTransport;
import network.tiesdb.exception.TiesException;

/**
 * TiesDB transport daemon API.
 * 
 * <P>Defines common daemon controls of TiesDB transport.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public interface TiesTransportDaemon {

	void start() throws TiesException;

	void stop() throws TiesException;

	void init() throws TiesException;
	
	TiesTransport getTiesTransport() throws TiesException;
}
