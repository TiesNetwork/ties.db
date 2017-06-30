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
package network.ties.db.service;

import org.apache.cassandra.service.ElassandraDaemon;

/**
* TiedDB Daemon Main Class.
* 
* <P>Entry point to run TiesDB with underlying services.
*  
* @author Anton Filatov (filatov@ties.network)
*/
public class TiesDBDaemon extends ElassandraDaemon {
    public static void main(String[] args) {
    	ElassandraDaemon.main(args);
    }
}
