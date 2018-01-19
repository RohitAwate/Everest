/*
 * Copyright 2018 Rohit Awate.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rohitawate.restaurant.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 *
 * @author Rohit Awate
 */
public class RequestManager {

	private final Client client;

	public RequestManager() {
		client = ClientBuilder.newClient();
	}

	public String get(URL url) throws MalformedURLException, IOException {
		String responseBody;
		WebTarget target = client.target(url.toString());

		Response response = target.request().get();
		String type = (String) response.getHeaders().getFirst("Content-type");
		System.out.println(type);
		responseBody = response.readEntity(String.class);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		
		switch (type) {
			case "application/json":
				JsonNode node = mapper.readTree(responseBody);
				responseBody = mapper.writeValueAsString(node);
				break;
			case "application/xml":
				responseBody = mapper.writeValueAsString(responseBody);
				break;
		}

		return responseBody;
	}
}
