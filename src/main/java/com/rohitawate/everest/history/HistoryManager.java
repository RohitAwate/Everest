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

package com.rohitawate.everest.history;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohitawate.everest.controllers.state.DashboardState;
import com.rohitawate.everest.controllers.state.FieldState;
import com.rohitawate.everest.misc.EverestUtilities;
import com.rohitawate.everest.misc.Services;
import com.rohitawate.everest.settings.Settings;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private Connection conn;
    private JsonNode queries;
    private PreparedStatement statement;
    private HistorySaver historySaver = new HistorySaver();

    public HistoryManager() {
        try {
            File configFolder = new File("Everest/config/");
            if (!configFolder.exists())
                configFolder.mkdirs();

            conn = DriverManager.getConnection("jdbc:sqlite:Everest/config/history.sqlite");
            initDatabase();

        } catch (Exception E) {
            Services.loggingService.logSevere("Exception while initializing HistoryManager.", E, LocalDateTime.now());
        } finally {
            System.out.println("Connected to database.");
        }
    }

    /**
     * Creates and initializes the database with necessary tables if not already done.
     *
     * @throws IOException  - If unable to establish a connection to the database.
     * @throws SQLException - If invalid statement is executed on the database.
     */
    private void initDatabase() throws IOException, SQLException {
        // Read all queries from Queries.json
        InputStream queriesFile = getClass().getResourceAsStream("/sql/Queries.json");
        ObjectMapper mapper = new ObjectMapper();
        queries = mapper.readTree(queriesFile);

        statement =
                conn.prepareStatement(EverestUtilities.trimString(queries.get("createRequestsTable").toString()));
        statement.execute();

        statement =
                conn.prepareStatement(EverestUtilities.trimString(queries.get("createHeadersTable").toString()));
        statement.execute();

        statement =
                conn.prepareStatement(EverestUtilities.trimString(queries.get("createRequestContentMapTable").toString()));
        statement.execute();

        statement =
                conn.prepareStatement(EverestUtilities.trimString(queries.get("createBodiesTable").toString()));
        statement.execute();

        statement =
                conn.prepareStatement(EverestUtilities.trimString(queries.get("createTuplesTable").toString()));
        statement.execute();
    }

    // Method is made synchronized to allow only one database transaction at a time.

    /**
     * Saves the request to the database if it is not identical to one made exactly before it.
     *
     * @param state - The state of the Dashboard while making the request.
     */
    public synchronized void saveHistory(DashboardState state) {
        if (isDuplicate(state))
            return;

        historySaver.state = state;
        Services.singleExecutor.execute(historySaver);

        // Appends this history item to the HistoryTab
        Services.homeWindowController.addHistoryItem(state);
    }

    /**
     * Returns a list of all the recent requests.
     */
    public synchronized List<DashboardState> getHistory() {
        List<DashboardState> history = new ArrayList<>();
        try {
            // Loads the requests from the last x number of days, x being Settings.showHistoryRange
            statement = conn.prepareStatement(EverestUtilities.trimString(queries.get("selectRecentRequests").toString()));
            String historyStartDate = LocalDate.now().minusDays(Settings.showHistoryRange).toString();
            statement.setString(1, historyStartDate);

            ResultSet resultSet = statement.executeQuery();

            DashboardState state;
            while (resultSet.next()) {
                state = new DashboardState();

                state.target = resultSet.getString("Target");

                int requestID = resultSet.getInt("ID");
                state.headers = getRequestHeaders(requestID);
                state.params = getTuples(requestID, "Param");
                state.httpMethod = resultSet.getString("Type");

                if (!(state.httpMethod.equals("GET") || state.httpMethod.equals("DELETE"))) {
                    // Retrieves request body ContentType for querying corresponding table
                    statement = conn.prepareStatement(EverestUtilities.trimString(queries.get("selectRequestContentType").toString()));
                    statement.setInt(1, requestID);

                    ResultSet RS = statement.executeQuery();

                    String contentType = "";
                    if (RS.next())
                        contentType = RS.getString("ContentType");

                    state.contentType = contentType;

                    // Retrieves body from corresponding table
                    switch (contentType) {
                        case MediaType.TEXT_PLAIN:
                        case MediaType.APPLICATION_JSON:
                        case MediaType.APPLICATION_XML:
                        case MediaType.TEXT_HTML:
                        case MediaType.APPLICATION_OCTET_STREAM:
                            statement = conn.prepareStatement(EverestUtilities.trimString(queries.get("selectRequestBody").toString()));
                            statement.setInt(1, requestID);

                            RS = statement.executeQuery();

                            if (RS.next())
                                state.rawBody = RS.getString("Body");
                            break;
                        case MediaType.APPLICATION_FORM_URLENCODED:
                            state.urlStringTuples = getTuples(requestID, "String");
                            break;
                        case MediaType.MULTIPART_FORM_DATA:
                            state.formStringTuples = getTuples(requestID, "String");
                            state.formFileTuples = getTuples(requestID, "File");
                            break;
                    }
                }

                history.add(state);
            }
        } catch (SQLException e) {
            Services.loggingService.logWarning("Database error.", e, LocalDateTime.now());
        }
        return history;
    }

    private ArrayList<FieldState> getRequestHeaders(int requestID) {
        ArrayList<FieldState> headers = new ArrayList<>();

        try {
            PreparedStatement statement =
                    conn.prepareStatement(EverestUtilities.trimString(queries.get("selectRequestHeaders").toString()));
            statement.setInt(1, requestID);

            ResultSet RS = statement.executeQuery();

            String key, value;
            boolean checked;
            while (RS.next()) {
                key = RS.getString("Key");
                value = RS.getString("Value");
                checked = RS.getBoolean("Checked");
                headers.add(new FieldState(key, value, checked));
            }
        } catch (SQLException e) {
            Services.loggingService.logWarning("Database error.", e, LocalDateTime.now());
        }
        return headers;
    }

    /**
     * @param requestID Database ID of the request whose tuples are needed.
     * @param type      Type of tuples needed ('String', 'File' or 'Param')
     * @return tuples - Map of tuples of corresponding type
     */
    private ArrayList<FieldState> getTuples(int requestID, String type) {
        if (!(type.equals("String") || type.equals("File") || type.equals("Param")))
            return null;

        ArrayList<FieldState> tuples = new ArrayList<>();

        try {
            PreparedStatement statement =
                    conn.prepareStatement(EverestUtilities.trimString(queries.get("selectTuples").toString()));
            statement.setInt(1, requestID);
            statement.setString(2, type);

            ResultSet RS = statement.executeQuery();

            String key, value;
            boolean checked;
            while (RS.next()) {
                key = RS.getString("Key");
                value = RS.getString("Value");
                checked = RS.getBoolean("Checked");
                tuples.add(new FieldState(key, value, checked));
            }
        } catch (SQLException e) {
            Services.loggingService.logWarning("Database error.", e, LocalDateTime.now());
        }

        return tuples;
    }

    /**
     * Performs a comprehensive comparison of the new request with the one added last to the database.
     *
     * @param newState The new request.
     * @return true, if request is same as the last one in the database. false, otherwise.
     */
    private boolean isDuplicate(DashboardState newState) {
        try {
            statement = conn.prepareStatement(EverestUtilities.trimString(queries.get("selectMostRecentRequest").toString()));
            ResultSet RS = statement.executeQuery();

            int lastRequestID = -1;
            if (RS.next()) {
                if (!(newState.httpMethod.equals(RS.getString("Type"))) ||
                        !(newState.target.equals(RS.getString("Target"))) ||
                        !(LocalDate.now().equals(LocalDate.parse(RS.getString("Date")))))
                    return false;
                else
                    lastRequestID = RS.getInt("ID");
            }

            // This condition is observed when the database is empty
            if (lastRequestID == -1)
                return false;

            ArrayList<FieldState> states;

            // Checks for new or modified headers
            states = getRequestHeaders(lastRequestID);
            if (!areListsEqual(states, newState.headers))
                return false;

            // Checks for new or modified params
            states = getTuples(lastRequestID, "Param");
            if (!areListsEqual(states, newState.params))
                return false;

            if (!(newState.httpMethod.equals("GET") || newState.httpMethod.equals("DELETE"))) {
                switch (newState.contentType) {
                    case MediaType.TEXT_PLAIN:
                    case MediaType.APPLICATION_JSON:
                    case MediaType.APPLICATION_XML:
                    case MediaType.TEXT_HTML:
                    case MediaType.APPLICATION_OCTET_STREAM:
                        statement = conn.prepareStatement(EverestUtilities.trimString(queries.get("selectRequestBody").toString()));
                        statement.setInt(1, lastRequestID);

                        RS = statement.executeQuery();

                        if (RS.next())
                            if (!RS.getString("Body").equals(newState.rawBody))
                                return false;

                        break;
                    case MediaType.APPLICATION_FORM_URLENCODED:
                        // Checks for new or modified string tuples
                        states = getTuples(lastRequestID, "String");
                        return areListsEqual(states, newState.urlStringTuples);
                    case MediaType.MULTIPART_FORM_DATA:
                        // Checks for new or modified string tuples
                        states = getTuples(lastRequestID, "String");
                        boolean stringComparison = areListsEqual(states, newState.formStringTuples);

                        // Checks for new or modified file tuples
                        states = getTuples(lastRequestID, "File");
                        boolean fileComparison = areListsEqual(states, newState.formFileTuples);

                        return stringComparison && fileComparison;
                }
            }
        } catch (SQLException e) {
            Services.loggingService.logWarning("Database error.", e, LocalDateTime.now());
        } catch (NullPointerException NPE) {
            /*
                NPE is thrown by containsKey indicating that the key is not present in the database thereby
                classifying it as a non-duplicate request.
             */
            return false;
        }
        return true;
    }

    private static boolean areListsEqual(ArrayList<FieldState> firstList, ArrayList<FieldState> secondList) {
        if (firstList == null && secondList == null)
            return true;

        if ((firstList == null && secondList != null) ||
                (firstList != null && secondList == null))
            return false;

        for (FieldState state : secondList) {
            if (!firstList.contains(state))
                return false;
        }

        return true;
    }

    private class HistorySaver implements Runnable {
        private DashboardState state;

        @Override
        public void run() {
            try {
                statement =
                        conn.prepareStatement(EverestUtilities.trimString(queries.get("saveRequest").toString()));

                statement.setString(1, state.httpMethod);
                statement.setString(2, state.target);
                statement.setString(3, LocalDate.now().toString());

                statement.executeUpdate();

                // Get latest RequestID to insert into Headers table
                statement = conn.prepareStatement("SELECT MAX(ID) AS MaxID FROM Requests");

                ResultSet RS = statement.executeQuery();
                int requestID = -1;
                if (RS.next())
                    requestID = RS.getInt("MaxID");

                if (state.headers.size() > 0) {
                    // Saves request headers
                    statement = conn.prepareStatement(EverestUtilities.trimString(queries.get("saveHeader").toString()));

                    for (FieldState fieldState : state.headers) {
                        statement.setInt(1, requestID);
                        statement.setString(2, fieldState.key);
                        statement.setString(3, fieldState.value);
                        statement.setInt(4, fieldState.checked ? 1 : 0);

                        statement.executeUpdate();
                    }
                }

                if (state.params.size() > 0) {
                    // Saves request parameters
                    statement = conn.prepareStatement(EverestUtilities.trimString(queries.get("saveTuple").toString()));
                    for (FieldState fieldState : state.params) {
                        statement.setInt(1, requestID);
                        statement.setString(2, "Param");
                        statement.setString(3, fieldState.key);
                        statement.setString(4, fieldState.value);
                        statement.setInt(5, fieldState.checked ? 1 : 0);

                        statement.executeUpdate();
                    }
                }

                if (!(state.httpMethod.equals("GET") || state.httpMethod.equals("DELETE"))) {
                    // Maps the request to its ContentType for faster retrieval
                    statement = conn.prepareStatement(EverestUtilities.trimString(queries.get("saveRequestContentPair").toString()));
                    statement.setInt(1, requestID);
                    statement.setString(2, state.contentType);

                    statement.executeUpdate();

                    // Determines where to fetch the body from, based on the ContentType
                    switch (state.contentType) {
                        case MediaType.TEXT_PLAIN:
                        case MediaType.APPLICATION_JSON:
                        case MediaType.APPLICATION_XML:
                        case MediaType.TEXT_HTML:
                        case MediaType.APPLICATION_OCTET_STREAM:
                            // Saves the body in case of raw content, or the file location in case of binary
                            statement = conn.prepareStatement(EverestUtilities.trimString(queries.get("saveBody").toString()));
                            statement.setInt(1, requestID);
                            statement.setString(2, state.rawBody);
                            statement.executeUpdate();
                            break;
                        case MediaType.APPLICATION_FORM_URLENCODED:
                            if (state.urlStringTuples.size() > 0) {
                                for (FieldState fieldState : state.urlStringTuples) {
                                    // Saves the string tuples
                                    statement = conn.prepareStatement(EverestUtilities.trimString(queries.get("saveTuple").toString()));
                                    statement.setInt(1, requestID);
                                    statement.setString(2, "String");
                                    statement.setString(3, fieldState.key);
                                    statement.setString(4, fieldState.value);
                                    statement.setInt(5, fieldState.checked ? 1 : 0);

                                    statement.executeUpdate();
                                }
                            }
                            break;
                        case MediaType.MULTIPART_FORM_DATA:
                            if (state.formStringTuples.size() > 0) {
                                for (FieldState fieldState : state.formStringTuples) {
                                    // Saves the string tuples
                                    statement = conn.prepareStatement(EverestUtilities.trimString(queries.get("saveTuple").toString()));
                                    statement.setInt(1, requestID);
                                    statement.setString(2, "String");
                                    statement.setString(3, fieldState.key);
                                    statement.setString(4, fieldState.value);
                                    statement.setInt(5, fieldState.checked ? 1 : 0);

                                    statement.executeUpdate();
                                }
                            }

                            if (state.formFileTuples.size() > 0) {
                                for (FieldState fieldState : state.formFileTuples) {
                                    // Saves the string tuples
                                    statement = conn.prepareStatement(EverestUtilities.trimString(queries.get("saveTuple").toString()));
                                    statement.setInt(1, requestID);
                                    statement.setString(2, "File");
                                    statement.setString(3, fieldState.key);
                                    statement.setString(4, fieldState.value);
                                    statement.setInt(5, fieldState.checked ? 1 : 0);

                                    statement.executeUpdate();
                                }
                            }
                            break;
                    }
                }
            } catch (SQLException e) {
                Services.loggingService.logWarning("Database error.", e, LocalDateTime.now());
            }
        }
    }
}
