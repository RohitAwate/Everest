package com.rohitawate.everest.auth;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DigestAuthProvider implements AuthProvider {

    private String url;
    private final String method;
    private String username;
    private String password;
    private boolean enabled;

    private static MessageDigest digester;

    static {
        try {
            digester = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public DigestAuthProvider(String url, String method, String username, String password, boolean enabled) {
        this.url = url;
        this.method = method;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
    }

    @Override
    public String getAuthHeader() {
        StringBuilder header = new StringBuilder();
        header.append("Digest ");

        try {
            URL digestURL = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) digestURL.openConnection();
            String nonceHeader = conn.getHeaderField("WWW-Authenticate");

            Pattern digestPattern = Pattern.compile("(\\w+)[:=] ?\"?([^\" ,]+)\"?");
            Matcher matcher = digestPattern.matcher(nonceHeader);

            String realm = null;
            String nonce = null;
            while (matcher.find()) {
                switch (matcher.group(1)) {
                    case "realm":
                        realm = matcher.group(2);
                        break;
                    case "nonce":
                        nonce = matcher.group(2);
                        break;
                }
            }

            String ha1 = getMD5Hash(username + ":" + realm + ":" + password);
            String ha2 = getMD5Hash(method + ":" + digestURL.getPath());
            String response = getMD5Hash(ha1 + ":" + nonce + ":" + ha2);

            header.append("username=\"");
            header.append(username);
            header.append("\", realm=\"");
            header.append(realm);
            header.append("\", nonce=\"");
            header.append(nonce);
            header.append("\", uri=\"");
            header.append(digestURL.getPath());
            header.append("\", response=\"");
            header.append(response);
            header.append("\"");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return header.toString();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private static String getMD5Hash(String input) {
        digester.update(input.getBytes());
        return DatatypeConverter.printHexBinary(digester.digest()).toLowerCase();
    }
}
