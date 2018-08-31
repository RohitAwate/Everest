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

import com.rohitawate.everest.logging.LoggingService;
import com.rohitawate.everest.models.requests.HTTPConstants;
import com.rohitawate.everest.settings.Settings;
import com.rohitawate.everest.state.ComposerState;
import com.rohitawate.everest.state.FieldState;
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
                "CREATE TABLE IF NOT EXISTS Requests(ID INTEGER PRIMARY KEY, Type TEXT NOT NULL, Target TEXT NOT NULL, Date TEXT NOT NULL)",
                "CREATE TABLE IF NOT EXISTS RequestContentMap(RequestID INTEGER, ContentType TEXT NOT NULL, FOREIGN KEY(RequestID) REFERENCES Requests(ID))",
                "CREATE TABLE IF NOT EXISTS Bodies(RequestID INTEGER, Type TEXT NOT NULL CHECK(Type IN ('application/json', 'application/xml', 'text/html', 'text/plain')), Body TEXT NOT NULL, FOREIGN KEY(RequestID) REFERENCES Requests(ID))",
                "CREATE TABLE IF NOT EXISTS FilePaths(RequestID INTEGER, Path TEXT NOT NULL, FOREIGN KEY(RequestID) REFERENCES Requests(ID))",
                "CREATE TABLE IF NOT EXISTS Tuples(RequestID INTEGER, Type TEXT NOT NULL CHECK(Type IN ('Header', 'Param', 'URLString', 'FormString', 'File')), Key TEXT NOT NULL, Value TEXT NOT NULL, Checked INTEGER CHECK (Checked IN (0, 1)), FOREIGN KEY(RequestID) REFERENCES Requests(ID))",
                "CREATE TABLE IF NOT EXISTS SimpleAuthCredentials(RequestID INTEGER, Type TEXT NOT NULL, Username TEXT NOT NULL, Password TEXT NOT NULL, Enabled INTEGER CHECK (Enabled IN (1, 0)), FOREIGN KEY(RequestID) REFERENCES Requests(ID))"
        };

        private static final String SAVE_REQUEST = "INSERT INTO Requests(Type, Target, Date) VALUES(?, ?, ?)";
        private static final String SAVE_REQUEST_CONTENT_PAIR = "INSERT INTO RequestContentMap(RequestID, ContentType) VALUES(?, ?)";
        private static final String SAVE_BODY = "INSERT INTO Bodies(RequestID, Body, Type) VALUES(?, ?, ?)";
        private static final String SAVE_FILE_PATH = "INSERT INTO FilePaths(RequestID, Path) VALUES(?, ?)";
        private static final String SAVE_TUPLE = "INSERT INTO Tuples(RequestID, Type, Key, Value, Checked) VALUES(?, ?, ?, ?, ?)";
        private static final String SAVE_SIMPLE_AUTH_CREDENTIALS = "INSERT INTO SimpleAuthCredentials(RequestID, Type, Username, Password, Enabled) VALUES(?, ?, ?, ?, ?)";
        private static final String SELECT_RECENT_REQUESTS = "SELECT * FROM Requests WHERE Requests.Date > ?";
        private static final String SELECT_REQUEST_CONTENT_TYPE = "SELECT ContentType FROM RequestContentMap WHERE RequestID == ?";
        private static final String SELECT_REQUEST_BODY = "SELECT Body, Type FROM Bodies WHERE RequestID == ?";
        private static final String SELECT_FILE_PATH = "SELECT Path FROM FilePaths WHERE RequestID == ?";
        private static final String SELECT_SIMPLE_AUTH_CREDENTIALS = "SELECT * FROM SimpleAuthCredentials WHERE RequestID == ? AND Type == ?";
        private static final String SELECT_TUPLES_BY_TYPE = "SELECT * FROM Tuples WHERE RequestID == ? AND Type == ?";
        private static final String SELECT_MOST_RECENT_REQUEST = "SELECT * FROM Requests ORDER BY ID DESC LIMIT 1";
    }

    private static final String ID = "ID";
    private static final String HEADER = "Header";
    private static final String PARAM = "Param";
    private static final String URL_STRING = "URLString";
    private static final String FORM_STRING = "FormString";
    private static final String FILE = "File";
    private static final String BASIC = "Basic";
    private static final String DIGEST = "Digest";

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
        statement.setString(3, LocalDate.now().toString());
        statement.executeUpdate();

        // Get latest RequestID to insert into Headers table
        statement = conn.prepareStatement("SELECT MAX(ID) AS MaxID FROM Requests");

        ResultSet RS = statement.executeQuery();
        int requestID = -1;
        if (RS.next())
            requestID = RS.getInt("MaxID");

        saveTuple(newState.headers, HEADER, requestID);
        saveTuple(newState.params, PARAM, requestID);

        saveSimpleAuthCredentials(requestID, BASIC, newState.basicUsername, newState.basicPassword, newState.basicEnabled);
        saveSimpleAuthCredentials(requestID, DIGEST, newState.digestUsername, newState.digestPassword, newState.digestEnabled);

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
        // Loads the requests from the last x number of days, x being Settings.showHistoryRange
        statement = conn.prepareStatement(Queries.SELECT_RECENT_REQUESTS);
        String historyStartDate = LocalDate.now().minusDays(Settings.showHistoryRange).toString();
        statement.setString(1, historyStartDate);

        ResultSet resultSet = statement.executeQuery();

        ComposerState state;
        while (resultSet.next()) {
            state = new ComposerState();

            state.target = resultSet.getString("Target");

            int requestID = resultSet.getInt(ID);
            state.headers = getTuples(requestID, HEADER);
            state.params = getTuples(requestID, PARAM);
            state.httpMethod = resultSet.getString("Type");
            getSimpleAuthCredentials(state, requestID, BASIC);
            getSimpleAuthCredentials(state, requestID, DIGEST);

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

    private void getSimpleAuthCredentials(ComposerState state, int requestID, String type) throws SQLException {
        if (!(type.equals(BASIC) || type.equals(DIGEST)))
            return;

        statement = conn.prepareStatement(Queries.SELECT_SIMPLE_AUTH_CREDENTIALS);
        statement.setInt(1, requestID);
        statement.setString(2, type);

        ResultSet RS = statement.executeQuery();

        if (RS.next()) {
            if (type.equals(BASIC)) {
                state.basicUsername = RS.getString("Username");
                state.basicPassword = RS.getString("Password");
                state.basicEnabled = RS.getInt("Enabled") == 1;
            } else if (type.equals(DIGEST)) {
                state.digestUsername = RS.getString("Username");
                state.digestPassword = RS.getString("Password");
                state.digestEnabled = RS.getInt("Enabled") == 1;
            }
        } else {
            String empty = "";
            state.basicUsername = empty;
            state.basicPassword = empty;
            state.basicEnabled = false;

            state.digestUsername = empty;
            state.digestPassword = empty;
            state.digestEnabled = false;
        }
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
            }

            getSimpleAuthCredentials(lastRequest, requestID, BASIC);
            getSimpleAuthCredentials(lastRequest, requestID, DIGEST);

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
                for (FieldState fieldState : tuples) {
                    statement = conn.prepareStatement(Queries.SAVE_TUPLE);
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