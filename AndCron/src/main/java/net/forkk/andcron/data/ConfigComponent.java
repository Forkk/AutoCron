/*
 * Copyright 2013 Andrew Okin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.forkk.andcron.data;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Interface that provides access to the underlying data structure of components such as
 * automations, rules, actions, and exceptions.
 */
public interface ConfigComponent
{
    /**
     * @return The user-given name for this component.
     */
    public abstract String getName();

    /**
     * @return The user-given description for this component.
     */
    public abstract String getDescription();

    /**
     * Reads the component's settings from the given JSON object.
     *
     * @param object
     *         The JSON object to read settings from.
     */
    public abstract void readFromJSONObject(JSONObject object)
            throws JSONException;

    /**
     * Writes the component's settings to a JSON object and returns it.
     *
     * @param object
     *         The JSON object to write settings to.
     */
    public abstract void writeToJSONObject(JSONObject object)
    throws JSONException;
}
