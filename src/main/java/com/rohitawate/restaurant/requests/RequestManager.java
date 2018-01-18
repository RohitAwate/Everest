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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Rohit Awate
 */
public class RequestManager {
	public String get(URL target) throws MalformedURLException, IOException {
		String response = "";
		
		HttpURLConnection conn = (HttpURLConnection) target.openConnection();
		conn.setRequestMethod("GET");
	
		InputStream responseStream = conn.getInputStream();
		BufferedReader responseReader =
				new BufferedReader(new InputStreamReader(responseStream));
		
		String line;
		while ((line = responseReader.readLine()) != null)
			response += line + "\n";
		
		return response;
	}
}
