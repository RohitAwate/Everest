package com.rohitawate.everest.server.mock;

import com.rohitawate.everest.models.requests.HTTPConstants;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

class MockServerTest {
    @Test
    void start() throws IOException {
        MockService service = new MockService("Summit", "/api", 9090);
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