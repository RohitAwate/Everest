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

import java.util.LinkedList;

public class RequestManagersPool {
    private LinkedList<GETRequestManager> getManagers;
    private LinkedList<DataDispatchRequestManager> dataManagers;
    private LinkedList<DELETERequestManager> deleteManagers;

    public GETRequestManager get() {
        if (getManagers == null) {
            GETRequestManager newManager = new GETRequestManager();

            new Thread(() -> {
                getManagers = new LinkedList<>();
                getManagers.add(newManager);
            }).start();

            return newManager;
        } else {
            for (GETRequestManager getManager : getManagers) {
                if (!getManager.isRunning())
                    return getManager;
            }

            GETRequestManager newManager = new GETRequestManager();
            getManagers.add(newManager);

            return newManager;
        }
    }

    public DataDispatchRequestManager data() {
        if (dataManagers == null) {
            DataDispatchRequestManager newManager = new DataDispatchRequestManager();

            new Thread(() -> {
                dataManagers = new LinkedList<>();
                dataManagers.add(newManager);
            }).start();

            return newManager;
        } else {
            for (DataDispatchRequestManager dataManager : dataManagers) {
                if (!dataManager.isRunning())
                    return dataManager;
            }

            DataDispatchRequestManager newManager = new DataDispatchRequestManager();
            dataManagers.add(newManager);

            return newManager;
        }
    }

    public DELETERequestManager delete() {
        if (deleteManagers == null) {
            DELETERequestManager newManager = new DELETERequestManager();

            new Thread(() -> {
                deleteManagers = new LinkedList<>();
                deleteManagers.add(newManager);
            }).start();

            return newManager;
        } else {
            for (DELETERequestManager deleteManager : deleteManagers) {
                if (!deleteManager.isRunning())
                    return deleteManager;
            }

            DELETERequestManager newManager = new DELETERequestManager();
            deleteManagers.add(newManager);

            return newManager;
        }
    }
}
