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
import com.rohitawate.restaurant.models.requests.DELETERequest;
import com.rohitawate.restaurant.models.requests.DataDispatchRequest;
import com.rohitawate.restaurant.models.requests.GETRequest;
import com.rohitawate.restaurant.models.requests.RestaurantRequest;
import com.rohitawate.restaurant.util.json.JSONUtils;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.Map;

public class HistoryManager {
    private Connection conn;
    private JsonNode queries;
    private PreparedStatement statement;

    public HistoryManager() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:requests.sqlite");

            // Read all queries from Queries.json
            File queriesFile = new File(getClass().getResource("/sql/Queries.json").toURI());
            ObjectMapper mapper = new ObjectMapper();
            queries = mapper.readTree(queriesFile);

            statement =
                    conn.prepareStatement(JSONUtils.trimString(queries.get("createRequestsTable").toString()));
            statement.execute();

            statement =
                    conn.prepareStatement(JSONUtils.trimString(queries.get("createHeadersTable").toString()));
            statement.execute();
        } catch (Exception E) {
            E.printStackTrace();
        } finally {
            System.out.println("Connected to database.");
        }
    }

    // Method is made synchronized to allow only one database transaction at a time.
    public synchronized void saveHistory(RestaurantRequest request) {
        try {
            statement =
                    conn.prepareStatement(JSONUtils.trimString(queries.get("saveRequest").toString()));

            // Determines the request type
            if (request.getClass() == GETRequest.class)
                statement.setString(1, "GET");
            else if (request.getClass() == DataDispatchRequest.class) {
                if (((DataDispatchRequest) request).getRequestType().equals("POST"))
                    statement.setString(1, "POST");
                else
                    statement.setString(1, "PUT");
            } else if (request.getClass() == DELETERequest.class)
                statement.setString(1, "DELETE");

            statement.setString(2, String.valueOf(request.getTarget()));
            statement.setString(3, LocalDate.now().toString());

            statement.executeUpdate();

            if (request.getHeaders().size() > 0) {
                // Get latest RequestID to insert into Headers table
                statement = conn.prepareStatement("SELECT MAX(ID) AS MaxID FROM Requests");

                ResultSet RS = statement.executeQuery();
                int requestID = -1;
                if (RS.next())
                    requestID = RS.getInt("MaxID");

                statement = conn.prepareStatement(JSONUtils.trimString(queries.get("saveHeader").toString()));
                for (Map.Entry entry : request.getHeaders().entrySet()) {
                    statement.setInt(1, requestID);
                    statement.setString(2, entry.getKey().toString());
                    statement.setString(3, entry.getValue().toString());

                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
