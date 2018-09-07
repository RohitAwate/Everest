package com.rohitawate.everest.auth.oauth2.code;

import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.misc.EverestUtilities;
import spark.Spark;

import java.awt.*;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;

public class BrowserCapturer implements AuthorizationGrantCapturer {
    private String authGrant;

    private void startServer() {
        Spark.port(52849);
        Spark.staticFiles.location("/assets");

        Spark.get("/granted", (req, res) -> {
            authGrant = req.queryParams("code");
            if (authGrant != null) {
                InputStream stream = getClass().getResourceAsStream("/templates/AuthorizationGrantedPage.html");
                return EverestUtilities.readFile(stream);
            } else {
                return "Not authorized";
            }
        });
    }

    private static void openLinkInBrowser(String url) {
        if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    LoggingService.logWarning("Invalid URL encountered while opening in browser.", ex, LocalDateTime.now());
                }
            }).start();
        }
    }

    @Override
    public String getAuthorizationGrant() {
        return null;
    }
}
