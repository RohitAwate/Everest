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
package com.rohitawate.restaurant.requestsmanager;

import com.rohitawate.restaurant.models.requests.RestaurantRequest;
import com.rohitawate.restaurant.models.responses.RestaurantResponse;
import javafx.concurrent.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public abstract class RequestManager extends Service<RestaurantResponse> {
    final Client client;
    RestaurantRequest request;

    RequestManager() {
        client = ClientBuilder.newClient();
    }

    public void setRequest(RestaurantRequest request) {
        this.request = request;
    }
}
