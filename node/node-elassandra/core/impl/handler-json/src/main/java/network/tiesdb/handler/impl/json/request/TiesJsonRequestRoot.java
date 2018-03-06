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
package network.tiesdb.handler.impl.json.request;

/**
 * Root of TiesDB JSON request.
 * 
 * <P>Any class implementing this interface can be a JSON request root.
 *  
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesJsonRequestRoot {

	public enum RequestType {
		INSERT, SELECT
	}

	private Object request;
	private RequestType type;

	private void setRequest(Object request, RequestType type) {
		this.request = request;
		this.type = type;
	}

	public void setInsert(TiesJsonRequestInsert request) {
		setRequest(request, RequestType.INSERT);
	}

	public void setSelect(TiesJsonRequestSelect request) {
		setRequest(request, RequestType.SELECT);
	}

	public Object getRequest() {
		return request;
	}

	public RequestType getType() {
		return type;
	}

	public String getInnerType() {
		return request.getClass().getName();
	}

}
