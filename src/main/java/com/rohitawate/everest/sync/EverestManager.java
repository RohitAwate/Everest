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

public interface EverestManager {
    /**
     * Returns the identifier for the Manager. Preferably, use the source as the identifier.
     * This identifier may be used in the UI, hence, do not add the type of Manager as a suffix.
     *
     * For example, a manager syncing preferences from Summit would be called 'Summit', not 'Summit Preferences Manager'.
     */
    String getIdentifier();
}
