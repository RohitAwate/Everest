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

package com.rohitawate.everest.requestmanager;

import java.util.ArrayList;

/**
 * Provides a dynamically-growing pool RequestManagers used by Everest.
 * <p>
 * The manager() method when invoked, searches the pool linearly.
 * The first RequestManager which is not currently running will be
 * returned to the caller. If all the managers in the pool are running,
 * a new one will be created, added to the pool, and returned.
 */
public class RequestManagersPool {
    private static ArrayList<RequestManager> pool = new ArrayList<>();

    public static RequestManager manager() {
        for (RequestManager manager: pool) {
            if (!manager.isRunning()) {
                manager.reset();
                return manager;
            }
        }

        RequestManager newManager = new RequestManager();
        pool.add(newManager);

        return newManager;
    }
}
