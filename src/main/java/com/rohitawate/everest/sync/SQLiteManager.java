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

package com.rohitawate.everest.sync;

import com.rohitawate.everest.Main;
import com.rohitawate.everest.auth.AuthMethod;
import com.rohitawate.everest.auth.oauth2.AccessToken;
import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.models.requests.HTTPConstants;
import com.rohitawate.everest.state.*;
import javafx.util.Pair;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class SQLiteManager implements DataManager {
    private Connection conn;
    private PreparedStatement statement;

    private static class Queries {
        private static final String[] CREATE_QUERIES = {
                "CREATE TABLE IF NOT EXISTS Requests(ID INTEGER PRIMARY KEY, Type TEXT NOT NULL, Target TEXT NOT NULL, AuthMethod TEXT, Date TEXT NOT NULL)",
                "CREATE TABLE IF NOT EXISTS RequestContentMap(RequestID INTEGER, ContentType TEXT NOT NULL, FOREIGN KEY(RequestID) REFERENCES Requests(ID))",
                "CREATE TABLE IF NOT EXISTS Bodies(RequestID INTEGER, Type TEXT NOT NULL CHECK(Type IN ('application/json', 'application/xml', 'text/html', 'text/plain')), Body TEXT NOT NULL, FOREIGN KEY(RequestID) REFERENCES Requests(ID))",
                "CREATE TABLE IF NOT EXISTS FilePaths(RequestID INTEGER, Path TEXT NOT NULL, FOREIGN KEY(RequestID) REFERENCES Requests(ID))",
                "CREATE TABLE IF NOT EXISTS Tuples(RequestID INTEGER, Type TEXT NOT NULL CHECK(Type IN ('Header', 'Param', 'URLString', 'FormString', 'File')), Key TEXT NOT NULL, Value TEXT NOT NULL, Checked INTEGER CHECK (Checked IN (0, 1)), FOREIGN KEY(RequestID) REFERENCES Requests(ID))",
                "CREATE TABLE IF NOT EXISTS SimpleAuthCredentials(RequestID INTEGER, Type TEXT NOT NULL, Username TEXT NOT NULL, Password TEXT NOT NULL, Enabled INTEGER CHECK (Enabled IN (1, 0)), FOREIGN KEY(RequestID) REFERENCES Requests(ID))",
                "CREATE TABLE IF NOT EXISTS AuthCodeCredentials(RequestID INTEGER, CaptureMethod TEXT NOT NULL CHECK (CaptureMethod IN ('System Browser', 'Integrated WebView')), AuthURL TEXT NOT NULL, AccessTokenURL TEXT NOT NULL, RedirectURL TEXT NOT NULL, ClientID TEXT NOT NULL, ClientSecret TEXT NOT NULL, Scope TEXT, State TEXT, HeaderPrefix TEXT, Enabled INTEGER CHECK(Enabled IN (0, 1)))",
                "CREATE TABLE IF NOT EXISTS OAuth2AccessTokens(RequestID INTEGER, AccessToken TEXT, RefreshToken TEXT, TokenType TEXT, TokenExpiry NUMBER, Scope TEXT, FOREIGN KEY(RequestID) REFERENCES Requests(ID))"
        };

        private static final String SAVE_REQUEST = "INSERT INTO Requests(Type, Target, AuthMethod, Date) VALUES(?, ?, ?, ?)";
        private static final String SAVE_REQUEST_CONTENT_PAIR = "INSERT INTO RequestContentMap(RequestID, ContentType) VALUES(?, ?)";
        private static final String SAVE_BODY = "INSERT INTO Bodies(RequestID, Body, Type) VALUES(?, ?, ?)";
        private static final String SAVE_FILE_PATH = "INSERT INTO FilePaths(RequestID, Path) VALUES(?, ?)";
        private static final String SAVE_TUPLE = "INSERT INTO Tuples(RequestID, Type, Key, Value, Checked) VALUES(?, ?, ?, ?, ?)";
        private static final String SAVE_SIMPLE_AUTH_CREDENTIALS = "INSERT INTO SimpleAuthCredentials(RequestID, Type, Username, Password, Enabled) VALUES(?, ?, ?, ?, ?)";
        private static final String SAVE_AUTH_CODE_CREDENTIALS = "INSERT INTO AuthCodeCredentials(RequestID, CaptureMethod, AuthURL, AccessTokenURL, RedirectURL, ClientID, ClientSecret, Scope, State, HeaderPrefix, Enabled) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        private static final String SAVE_OAUTH2_ACCESS_TOKEN = "INSERT INTO OAuth2AccessTokens(RequestID, AccessToken, RefreshToken, TokenType, TokenExpiry, Scope) VALUES(?, ?, ?, ?, ?, ?)";

        private static final String SELECT_RECENT_REQUESTS = "SELECT * FROM Requests WHERE Requests.Date > ?";
        private static final String SELECT_REQUEST_CONTENT_TYPE = "SELECT ContentType FROM RequestContentMap WHERE RequestID == ?";
        private static final String SELECT_REQUEST_BODY = "SELECT Body, Type FROM Bodies WHERE RequestID == ?";
        private static final String SELECT_FILE_PATH = "SELECT Path FROM FilePaths WHERE RequestID == ?";
        private static final String SELECT_SIMPLE_AUTH_CREDENTIALS = "SELECT * FROM SimpleAuthCredentials WHERE RequestID == ? AND Type == ?";
        private static final String SELECT_AUTH_CODE_CREDENTIALS = "SELECT * FROM AuthCodeCredentials WHERE RequestID == ?";
        private static final String SELECT_OAUTH2_ACCESS_TOKEN = "SELECT * FROM OAuth2AccessTokens WHERE RequestID == ?";
        private static final String SELECT_TUPLES_BY_TYPE = "SELECT * FROM Tuples WHERE RequestID == ? AND Type == ?";
        private static final String SELECT_MOST_RECENT_REQUEST = "SELECT * FROM Requests ORDER BY ID DESC LIMIT 1";
    }

    public SQLiteManager() {
        try {
            String configPath = "Everest/config/";
            File configFolder = new File(configPath);
            if (!configFolder.exists()) {
                if (configFolder.mkdirs())
                    LoggingService.logSevere("Unable to create directory: " + configPath, null, LocalDateTime.now());
            }

            conn = DriverManager.getConnection("jdbc:sqlite:Everest/config/history.sqlite");
            createDatabase();
            LoggingService.logInfo("Connected to database.", LocalDateTime.now());
        } catch (Exception E) {
            LoggingService.logSevere("Exception while initializing SQLiteManager.", E, LocalDateTime.now());
        }
    }

    /**
     * Creates and initializes the database with necessary tables if not already done.
     */
    private void createDatabase() throws SQLException {
        for (String query : Queries.CREATE_QUERIES) {
            statement = conn.prepareStatement(query);
            statement.execute();
        }
    }

    /**
     * Saves the request to the database if it is not identical to one made exactly before it.
     * Method is synchronized to allow only one database transaction at a time.
     *
     * @param newState - The state of the Dashboard while making the request.
     */
    @Override
    public synchronized void saveState(ComposerState newState) throws SQLException {
        statement = conn.prepareStatement(Queries.SAVE_REQUEST);

        statement.setString(1, newState.httpMethod);
        statement.setString(2, newState.target);
        statement.setString(3, newState.authMethod);
        statement.setString(4, LocalDate.now().toString());
        statement.executeUpdate();

        // Get latest RequestID to insert into Headers table
        statement = conn.prepareStatement("SELECT MAX(ID) AS MaxID FROM Requests");

        ResultSet RS = statement.executeQuery();
        int requestID = -1;
        if (RS.next())
            requestID = RS.getInt("MaxID");

        saveTuple(newState.headers, HEADER, requestID);
        saveTuple(newState.params, PARAM, requestID);

        saveSimpleAuthCredentials(requestID, AuthMethod.BASIC, newState.basicAuthState.username,
                newState.basicAuthState.password, newState.basicAuthState.enabled);
        saveSimpleAuthCredentials(requestID, AuthMethod.DIGEST, newState.digestAuthState.username,
                newState.digestAuthState.password, newState.digestAuthState.enabled);

        saveOAuth2Credentials(requestID, newState.oAuth2State);

        if (!(newState.httpMethod.equals(HTTPConstants.GET) || newState.httpMethod.equals(HTTPConstants.DELETE))) {
            // Maps the request to its ContentType for faster retrieval
            statement = conn.prepareStatement(Queries.SAVE_REQUEST_CONTENT_PAIR);
            statement.setInt(1, requestID);
            statement.setString(2, newState.contentType);
            statement.executeUpdate();

            statement = conn.prepareStatement(Queries.SAVE_BODY);
            statement.setInt(1, requestID);
            statement.setString(2, newState.rawBody);
            statement.setString(3, newState.rawBodyBoxValue);
            statement.executeUpdate();

            statement = conn.prepareStatement(Queries.SAVE_FILE_PATH);
            statement.setInt(1, requestID);
            statement.setString(2, newState.binaryFilePath);
            statement.executeUpdate();

            saveTuple(newState.urlStringTuples, URL_STRING, requestID);
            saveTuple(newState.formStringTuples, FORM_STRING, requestID);
            saveTuple(newState.formFileTuples, FILE, requestID);
        }
    }

    private void saveOAuth2Credentials(int requestID, OAuth2State oAuth2State) throws SQLException {
        saveAuthCodeCredentials(requestID, oAuth2State.codeState);
        saveOAuth2AccessToken(requestID, oAuth2State.codeState.accessToken);
    }

    private void saveOAuth2AccessToken(int requestID, AccessToken accessToken) throws SQLException {
        if (accessToken == null) {
            return;
        }

        statement = conn.prepareStatement(Queries.SAVE_OAUTH2_ACCESS_TOKEN);
        statement.setInt(1, requestID);
        statement.setString(2, accessToken.getAccessToken());
        statement.setString(3, accessToken.getRefreshToken());
        statement.setString(4, accessToken.getTokenType());
        statement.setInt(5, accessToken.getExpiresIn());
        statement.setString(6, accessToken.getScope());

        statement.executeUpdate();
    }

    private void saveAuthCodeCredentials(int requestID, AuthorizationCodeState state) throws SQLException {
        // TODO: Check if these checks are enough
        if (state.authURL.isEmpty() && state.accessTokenURL.isEmpty()
                && state.clientID.isEmpty() && state.clientSecret.isEmpty()) {
            return;
        }

        statement = conn.prepareStatement(Queries.SAVE_AUTH_CODE_CREDENTIALS);
        statement.setInt(1, requestID);
        statement.setString(2, state.grantCaptureMethod);
        statement.setString(3, state.authURL);
        statement.setString(4, state.accessTokenURL);
        statement.setString(5, state.redirectURL);
        statement.setString(6, state.clientID);
        statement.setString(7, state.clientSecret);
        statement.setString(8, state.scope);
        statement.setString(9, state.state);
        statement.setString(10, state.headerPrefix);
        statement.setInt(11, state.enabled ? 1 : 0);

        statement.executeUpdate();
    }

    private void saveSimpleAuthCredentials(int requestID,
                                           String type,
                                           String username,
                                           String password,
                                           boolean enabled) throws SQLException {
        if (username == null || username.isEmpty() || password == null || password.isEmpty())
            return;

        statement = conn.prepareStatement(Queries.SAVE_SIMPLE_AUTH_CREDENTIALS);
        statement.setInt(1, requestID);
        statement.setString(2, type);
        statement.setString(3, username);
        statement.setString(4, password);
        statement.setInt(5, enabled ? 1 : 0);

        statement.executeUpdate();
    }

    /**
     * Returns a list of all the recent requests.
     */
    @Override
    public synchronized List<ComposerState> getHistory() throws SQLException {
        List<ComposerState> history = new ArrayList<>();
        // Loads the requests from the last x number of days, x being Preferences.showHistoryRange
        statement = conn.prepareStatement(Queries.SELECT_RECENT_REQUESTS);
        String historyStartDate = LocalDate.now().minusDays(Main.preferences.appearance.showHistoryRange).toString();
        statement.setString(1, historyStartDate);

        ResultSet resultSet = statement.executeQuery();

        ComposerState state;
        while (resultSet.next()) {
            state = new ComposerState();

            state.target = resultSet.getString("Target");
            state.authMethod = resultSet.getString(AUTH_METHOD);

            int requestID = resultSet.getInt(ID);
            state.headers = getTuples(requestID, HEADER);
            state.params = getTuples(requestID, PARAM);
            state.httpMethod = resultSet.getString("Type");
            state.basicAuthState = getSimpleAuthCredentials(requestID, AuthMethod.BASIC);
            state.digestAuthState = getSimpleAuthCredentials(requestID, AuthMethod.DIGEST);
            state.oAuth2State = getOAuth2State(requestID);

            if (!(state.httpMethod.equals(HTTPConstants.GET) || state.httpMethod.equals(HTTPConstants.DELETE))) {
                // Retrieves request body ContentType for querying corresponding table
                state.contentType = getRequestContentType(requestID);

                Pair<String, String> rawBodyAndType = getRequestBody(requestID);

                if (rawBodyAndType != null) {
                    state.rawBody = rawBodyAndType.getKey();
                    state.rawBodyBoxValue = rawBodyAndType.getValue();
                }

                state.binaryFilePath = getFilePath(requestID);

                state.urlStringTuples = getTuples(requestID, URL_STRING);
                state.formStringTuples = getTuples(requestID, FORM_STRING);
                state.formFileTuples = getTuples(requestID, FILE);
            }

            history.add(state);
        }

        return history;
    }

    private SimpleAuthState getSimpleAuthCredentials(int requestID, String type) throws SQLException {
        if (!(type.equals(AuthMethod.BASIC) || type.equals(AuthMethod.DIGEST)))
            return null;

        SimpleAuthState state = new SimpleAuthState();

        statement = conn.prepareStatement(Queries.SELECT_SIMPLE_AUTH_CREDENTIALS);
        statement.setInt(1, requestID);
        statement.setString(2, type);

        ResultSet RS = statement.executeQuery();

        if (RS.next()) {
            if (type.equals(AuthMethod.BASIC)) {
                state.username = RS.getString("Username");
                state.password = RS.getString("Password");
                state.enabled = RS.getInt("Enabled") == 1;
            } else if (type.equals(AuthMethod.DIGEST)) {
                state.username = RS.getString("Username");
                state.password = RS.getString("Password");
                state.enabled = RS.getInt("Enabled") == 1;
            }
        } else {
            String empty = "";
            state.username = empty;
            state.password = empty;
            state.enabled = false;
        }

        return state;
    }

    private OAuth2State getOAuth2State(int requestID) throws SQLException {
        OAuth2State state = new OAuth2State();
        state.codeState = getAuthCodeCredentials(requestID);
        state.codeState.accessToken = getOAuth2AccessToken(requestID);

        return state;
    }

    private AccessToken getOAuth2AccessToken(int requestID) throws SQLException {
        statement = conn.prepareStatement(Queries.SELECT_OAUTH2_ACCESS_TOKEN);
        statement.setInt(1, requestID);

        ResultSet resultSet = statement.executeQuery();

        AccessToken accessToken = null;
        if (resultSet.next()) {
            accessToken = new AccessToken(
                    resultSet.getString("AccessToken"),
                    resultSet.getString("TokenType"),
                    resultSet.getInt("TokenExpiry"),
                    resultSet.getString("RefreshToken"),
                    resultSet.getString("Scope")
            );
        }

        return accessToken;
    }

    private AuthorizationCodeState getAuthCodeCredentials(int requestID) throws SQLException {
        statement = conn.prepareStatement(Queries.SELECT_AUTH_CODE_CREDENTIALS);
        statement.setInt(1, requestID);

        ResultSet resultSet = statement.executeQuery();

        AuthorizationCodeState state = new AuthorizationCodeState();
        if (resultSet.next()) {
            state.authURL = resultSet.getString("AuthURL");
            state.grantCaptureMethod = resultSet.getString("CaptureMethod");
            state.accessTokenURL = resultSet.getString("AccessTokenURL");
            state.redirectURL = resultSet.getString("RedirectURL");
            state.clientID = resultSet.getString("ClientID");
            state.clientSecret = resultSet.getString("ClientSecret");
            state.state = resultSet.getString("State");
            state.scope = resultSet.getString("Scope");
            state.headerPrefix = resultSet.getString("HeaderPrefix");
            state.enabled = resultSet.getInt("Enabled") == 1;
        }

        return state;
    }

    private String getRequestContentType(int requestID) throws SQLException {
        String contentType = null;

        statement = conn.prepareStatement(Queries.SELECT_REQUEST_CONTENT_TYPE);
        statement.setInt(1, requestID);

        ResultSet RS = statement.executeQuery();

        if (RS.next())
            contentType = RS.getString("ContentType");

        return contentType;
    }

    /**
     * @param requestID Database ID of the request whose tuples are needed.
     * @param type      Type of tuples needed ('URLString', 'FormString', 'File', 'Header' or 'Param')
     * @return fieldStates - List of FieldStates for the tuples
     */
    private List<FieldState> getTuples(int requestID, String type) throws SQLException {
        if (!(type.equals(FORM_STRING) || type.equals(URL_STRING) ||
                type.equals(FILE) || type.equals(PARAM) || type.equals(HEADER)))
            return null;

        ArrayList<FieldState> fieldStates = new ArrayList<>();

        PreparedStatement statement = conn.prepareStatement(Queries.SELECT_TUPLES_BY_TYPE);
        statement.setInt(1, requestID);
        statement.setString(2, type);

        ResultSet RS = statement.executeQuery();

        String key, value;
        boolean checked;
        while (RS.next()) {
            key = RS.getString("Key");
            value = RS.getString("Value");
            checked = RS.getBoolean("Checked");
            fieldStates.add(new FieldState(key, value, checked));
        }

        return fieldStates;
    }

    @Override
    public ComposerState getLastAdded() {
        ComposerState lastRequest = new ComposerState();
        try {
            statement = conn.prepareStatement(Queries.SELECT_MOST_RECENT_REQUEST);
            ResultSet RS = statement.executeQuery();

            int requestID = -1;
            if (RS.next()) {
                requestID = RS.getInt(ID);
                lastRequest.target = RS.getString("Target");
                lastRequest.httpMethod = RS.getString("Type");
                lastRequest.authMethod = RS.getString(AUTH_METHOD);
            }

            lastRequest.basicAuthState = getSimpleAuthCredentials(requestID, AuthMethod.BASIC);
            lastRequest.digestAuthState = getSimpleAuthCredentials(requestID, AuthMethod.DIGEST);
            lastRequest.oAuth2State = getOAuth2State(requestID);

            lastRequest.headers = getTuples(requestID, HEADER);
            lastRequest.params = getTuples(requestID, PARAM);
            lastRequest.urlStringTuples = getTuples(requestID, URL_STRING);
            lastRequest.formStringTuples = getTuples(requestID, FORM_STRING);
            lastRequest.formFileTuples = getTuples(requestID, FILE);

            lastRequest.contentType = getRequestContentType(requestID);

            lastRequest.binaryFilePath = getFilePath(requestID);

            Pair<String, String> rawBodyAndType = getRequestBody(requestID);

            if (rawBodyAndType != null) {
                lastRequest.rawBody = rawBodyAndType.getKey();
                lastRequest.rawBodyBoxValue = rawBodyAndType.getValue();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lastRequest;
    }

    private Pair<String, String> getRequestBody(int requestID) throws SQLException {
        statement = conn.prepareStatement(Queries.SELECT_REQUEST_BODY);
        statement.setInt(1, requestID);

        ResultSet RS = statement.executeQuery();

        if (RS.next()) {
            return new Pair<>(RS.getString("Body"), RS.getString("Type"));
        } else {
            return null;
        }
    }

    private String getFilePath(int requestID) throws SQLException {
        statement = conn.prepareStatement(Queries.SELECT_FILE_PATH);
        statement.setInt(1, requestID);

        ResultSet RS = statement.executeQuery();

        if (RS.next())
            return RS.getString("Path");
        else
            return null;
    }

    private void saveTuple(List<FieldState> tuples, String tupleType, int requestID) {
        if (tuples.size() > 0) {
            try {
                statement = conn.prepareStatement(Queries.SAVE_TUPLE);
                for (FieldState fieldState : tuples) {
                    statement.setInt(1, requestID);
                    statement.setString(2, tupleType);
                    statement.setString(3, fieldState.key);
                    statement.setString(4, fieldState.value);
                    statement.setInt(5, fieldState.checked ? 1 : 0);
                    statement.addBatch();
                }
                statement.executeBatch();
            } catch (SQLException e) {
                LoggingService.logSevere("Database error.", e, LocalDateTime.now());
            }
        }
    }

    @Override
    public String getIdentifier() {
        return "SQLite";
    }
}