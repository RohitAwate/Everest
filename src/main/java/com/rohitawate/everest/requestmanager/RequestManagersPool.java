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
 * Provides the various RequestManagers employed by Everest.
 * <p>
 * Pools are created as needed i.e. the first DELETE request
 * will create the pool of DELETERequestManagers. If a DELETE
 * request is never made, the pool won't be created. Same applies
 * for all other types of requests.
 * <p>
 * When demanding a RequestManager, the pool is checked linearly.
 * The first RequestManager which is not currently running will be
 * returned to the caller. If all the managers in the pool are running,
 * a new one is created, added to the pool, and returned.
 */
public class RequestManagersPool {
    private static ArrayList<GETRequestManager> getManagers;
    private static ArrayList<DataRequestManager> dataManagers;
    private static ArrayList<DELETERequestManager> deleteManagers;

    public static GETRequestManager get() {
        if (getManagers == null)
            getManagers = new ArrayList<>();

        for (GETRequestManager getManager : getManagers) {
            if (!getManager.isRunning()) {
                getManager.reset();
                return getManager;
            }
        }

        GETRequestManager newManager = new GETRequestManager();
        getManagers.add(newManager);

        return newManager;
    }

    public static DataRequestManager data() {
        if (dataManagers == null)
            dataManagers = new ArrayList<>();

        for (DataRequestManager dataManager : dataManagers) {
            if (!dataManager.isRunning()) {
                dataManager.reset();
                return dataManager;
            }
        }

        DataRequestManager newManager = new DataRequestManager();
        dataManagers.add(newManager);

        return newManager;
    }

    public static DELETERequestManager delete() {
        if (deleteManagers == null)
            deleteManagers = new ArrayList<>();

        for (DELETERequestManager deleteManager : deleteManagers) {
            if (!deleteManager.isRunning()) {
                deleteManager.reset();
                return deleteManager;
            }
        }

        DELETERequestManager newManager = new DELETERequestManager();
        deleteManagers.add(newManager);

        return newManager;
    }
}
