package com.rohitawate.everest.server.mock;

import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.preferences.Preferences;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

class MockServerTest {
    @Test
    void start() throws IOException {
        MockServer server = new MockServer();
        WebService service = new WebService("everest");
        Preferences preferences = new Preferences();
        Endpoint endpoint = new Endpoint("/everest", 200,
                EverestUtilities.jsonMapper.writeValueAsString(preferences), MediaType.APPLICATION_JSON);

        service.addEndpoint(endpoint);
        server.addService(service);
        server.start();

        while (server.isRunning()) ;
    }
}