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

package com.rohitawate.restaurant.util.history;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohitawate.restaurant.models.DashboardState;
import com.rohitawate.restaurant.models.requests.DELETERequest;
import com.rohitawate.restaurant.models.requests.DataDispatchRequest;
import com.rohitawate.restaurant.models.requests.GETRequest;
import com.rohitawate.restaurant.models.requests.RestaurantRequest;
import com.rohitawate.restaurant.util.Services;
import com.rohitawate.restaurant.util.json.JSONUtils;
import com.rohitawate.restaurant.util.settings.Settings;
import javafx.util.Pair;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryManager {
    private Connection conn;
    private JsonNode queries;
    private PreparedStatement statement;

    public HistoryManager() {
        try {
            File configFolder = new File("config/");
            if (!configFolder.exists())
                configFolder.mkdir();

            conn = DriverManager.getConnection("jdbc:sqlite:config/history.sqlite");

            // Read all queries from Queries.json
            InputStream queriesFile = getClass().getResourceAsStream("/sql/Queries.json");
            ObjectMapper mapper = new ObjectMapper();
            queries = mapper.readTree(queriesFile);

            statement =
                    conn.prepareStatement(JSONUtils.trimString(queries.get("createRequestsTable").toString()));
            statement.execute();

            statement =
                    conn.prepareStatement(JSONUtils.trimString(queries.get("createHeadersTable").toString()));
            statement.execute();

            statement =
                    conn.prepareStatement(JSONUtils.trimString(queries.get("createRequestContentMapTable").toString()));
            statement.execute();

            statement =
                    conn.prepareStatement(JSONUtils.trimString(queries.get("createBodiesTable").toString()));
            statement.execute();

            statement =
                    conn.prepareStatement(JSONUtils.trimString(queries.get("createTuplesTable").toString()));
            statement.execute();
        } catch (Exception E) {
            E.printStackTrace();
        } finally {
            System.out.println("Connected to database.");
        }
    }

    // Method is made synchronized to allow only one database transaction at a time.
    public synchronized void saveHistory(DashboardState state) {
        new Thread(() -> {
            try {
                statement =
                        conn.prepareStatement(JSONUtils.trimString(queries.get("saveRequest").toString()));

                statement.setString(1, state.getHttpMethod());
                statement.setString(2, String.valueOf(state.getTarget()));
                statement.setString(3, LocalDate.now().toString());

                statement.executeUpdate();

                if (state.getHeaders().size() > 0) {
                    // Get latest RequestID to insert into Headers table
                    statement = conn.prepareStatement("SELECT MAX(ID) AS MaxID FROM Requests");

                    ResultSet RS = statement.executeQuery();
                    int requestID = -1;
                    if (RS.next())
                        requestID = RS.getInt("MaxID");

                    // Saves request headers
                    statement = conn.prepareStatement(JSONUtils.trimString(queries.get("saveHeader").toString()));
                    for (Map.Entry entry : state.getHeaders().entrySet()) {
                        statement.setInt(1, requestID);
                        statement.setString(2, entry.getKey().toString());
                        statement.setString(3, entry.getValue().toString());

                        statement.executeUpdate();
                    }

                    if (state.getHttpMethod().equals("POST") || state.getHttpMethod().equals("PUT")) {
                        // Maps the request to its ContentType for faster recovery
                        statement = conn.prepareStatement(JSONUtils.trimString(queries.get("saveRequestContentPair").toString()));
                        statement.setInt(1, requestID);
                        statement.setString(2, state.getContentType());

                        statement.executeUpdate();

                        // Determines where to fetch the body from, based on the ContentType
                        switch (state.getContentType()) {
                            case MediaType.TEXT_PLAIN:
                            case MediaType.APPLICATION_JSON:
                            case MediaType.APPLICATION_XML:
                            case MediaType.TEXT_HTML:
                            case MediaType.APPLICATION_OCTET_STREAM:
                                // Saves the body in case of raw content, or the file location in case of binary
                                statement = conn.prepareStatement(JSONUtils.trimString(queries.get("saveBody").toString()));
                                statement.setInt(1, requestID);
                                statement.setString(2, state.getBody());
                                statement.executeUpdate();
                                break;
                            case MediaType.APPLICATION_FORM_URLENCODED:
                                for (Map.Entry<String, String> entry : state.getStringTuples().entrySet()) {
                                    // Saves the string tuples
                                    statement = conn.prepareStatement(JSONUtils.trimString(queries.get("saveTuple").toString()));
                                    statement.setInt(1, requestID);
                                    statement.setString(2, "String");
                                    statement.setString(3, entry.getKey());
                                    statement.setString(4, entry.getValue());

                                    statement.executeUpdate();
                                }
                                break;
                            case MediaType.MULTIPART_FORM_DATA:
                                for (Map.Entry<String, String> entry : state.getStringTuples().entrySet()) {
                                    // Saves the string tuples
                                    statement = conn.prepareStatement(JSONUtils.trimString(queries.get("saveTuple").toString()));
                                    statement.setInt(1, requestID);
                                    statement.setString(2, "String");
                                    statement.setString(3, entry.getKey());
                                    statement.setString(4, entry.getValue());

                                    statement.executeUpdate();
                                }

                                for (Map.Entry<String, String> entry : state.getFileTuples().entrySet()) {
                                    // Saves the file tuples
                                    statement = conn.prepareStatement(JSONUtils.trimString(queries.get("saveTuple").toString()));
                                    statement.setInt(1, requestID);
                                    statement.setString(2, "File");
                                    statement.setString(3, entry.getKey());
                                    statement.setString(4, entry.getValue());

                                    statement.executeUpdate();
                                }
                                break;
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
        // Appends this history item to the HistoryTab
        Services.homeWindowController.addHistoryItem(state);
    }

    public synchronized List<DashboardState> getHistory() {
        List<DashboardState> history = new ArrayList<>();
        try {
            // Loads the requests from the last x number of days, x being stored in Settings.showHistoryRange
            statement = conn.prepareStatement(JSONUtils.trimString(queries.get("selectRecentRequests").toString()));
            String historyStartDate = LocalDate.now().minusDays(Settings.showHistoryRange).toString();
            statement.setString(1, historyStartDate);

            ResultSet resultSet = statement.executeQuery();

            DashboardState state;
            while (resultSet.next()) {
                state = new DashboardState();

                try {
                    state.setTarget(resultSet.getString("Target"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                int requestID = resultSet.getInt("ID");
                state.setHeaders(getRequestHeaders(requestID));
                state.setHttpMethod(resultSet.getString("Type"));

                if (state.getHttpMethod().equals("POST") || state.getHttpMethod().equals("PUT")) {
                    // Retrieves request body ContentType for querying corresponding table
                    statement = conn.prepareStatement(JSONUtils.trimString(queries.get("selectRequestContentType").toString()));
                    statement.setInt(1, requestID);

                    ResultSet RS = statement.executeQuery();

                    String contentType = "";
                    if (RS.next())
                        contentType = RS.getString("ContentType");

                    state.setContentType(contentType);

                    // Retrieves body from corresponding table
                    switch (contentType) {
                        case MediaType.TEXT_PLAIN:
                        case MediaType.APPLICATION_JSON:
                        case MediaType.APPLICATION_XML:
                        case MediaType.TEXT_HTML:
                        case MediaType.APPLICATION_OCTET_STREAM:
                            statement = conn.prepareStatement(JSONUtils.trimString(queries.get("selectRequestBody").toString()));
                            statement.setInt(1, requestID);

                            RS = statement.executeQuery();

                            if (RS.next())
                                state.setBody(resultSet.getString("Body"));
                        break;
                        case MediaType.APPLICATION_FORM_URLENCODED:
                            state.setStringTuples(getTuples(requestID, "String"));
                            break;
                        case MediaType.MULTIPART_FORM_DATA:
                            state.setStringTuples(getTuples(requestID, "String"));
                            state.setFileTuples(getTuples(requestID, "Files"));
                            break;
                    }
                }

                history.add(state);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    private HashMap<String, String> getRequestHeaders(int requestID) {
        HashMap<String, String> headers = new HashMap<>();

        try {
            PreparedStatement statement =
                    conn.prepareStatement(JSONUtils.trimString(queries.get("selectRequestHeaders").toString()));
            statement.setInt(1, requestID);

            ResultSet RS = statement.executeQuery();

            String key, value;
            while (RS.next()) {
                key = RS.getString("Key");
                value = RS.getString("Value");
                headers.put(key, value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return headers;
    }

    /**
     *
     * @param requestID Database ID of the request whose tuples are needed.
     * @param type Type of tuples needed ('String' or 'File')
     * @return tuples - Map of tuples of corresponding type
     */
    private HashMap<String, String> getTuples(int requestID, String type) {
        if (!type.equals("String") || !type.equals("File"))
            return null;

        HashMap<String, String> tuples = new HashMap<>();

        try {
            PreparedStatement statement =
                    conn.prepareStatement(JSONUtils.trimString(queries.get("selectTuples").toString()));
            statement.setInt(1, requestID);
            statement.setString(2, type);

            ResultSet RS = statement.executeQuery();

            String key, value;
            while (RS.next()) {
                key = RS.getString("Key");
                value = RS.getString("Value");
                tuples.put(key, value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tuples;
    }
}
