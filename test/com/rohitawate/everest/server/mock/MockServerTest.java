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

package com.rohitawate.everest.server.mock;

import com.rohitawate.everest.models.requests.HTTPConstants;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

class MockServerTest {
    @Test
    void start() throws IOException {
        MockServer service = new MockServer("Summit", "/api", 9090);
        service.loggingEnabled = true;

        Endpoint endpoint = new Endpoint(HTTPConstants.GET, "/summit", 200,
                "{ \"name\": \"Rohit\", \"age\": 20 }", MediaType.APPLICATION_JSON);
        Endpoint endpoint2 = new Endpoint(HTTPConstants.GET, "/welcome", 200,
                "{ \"name\": \"Nolan\", \"age\": 48 }", MediaType.APPLICATION_JSON);

        service.addEndpoint(endpoint);
        service.addEndpoint(endpoint2);
        service.start();

        while (service.isRunning()) ;
    }
}