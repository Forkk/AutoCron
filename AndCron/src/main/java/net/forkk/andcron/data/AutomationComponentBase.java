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
 * Abstract base class for automation components that implements all of their core functionality.
 */
public abstract class AutomationComponentBase implements AutomationComponent
{
    protected String mName;

    protected String mDescription;

    protected Automation mAutomation;

    /**
     * Sets this component's parent. The parent is the automation that contains the rule.
     *
     * @param parent
     *         The rule's parent.
     */
    @Override
    public void setParent(Automation parent)
    {
        mAutomation = parent;
    }

    /**
     * Gets this component's parent automation.
     */
    protected Automation getParent()
    {
        return mAutomation;
    }

    /**
     * @return The user-given name for this component.
     */
    @Override
    public String getName()
    {
        return mName;
    }

    /**
     * @return The user-given description for this component.
     */
    @Override
    public String getDescription()
    {
        return mDescription;
    }

    /**
     * Reads the component's settings from the given JSON object.
     *
     * @param object
     *         The JSON object to read settings from.
     */
    @Override
    public void readFromJSONObject(JSONObject object)
            throws JSONException
    {
        mName = object.getString("name");
        mDescription = object.getString("description");
    }

    /**
     * Writes the component's settings to a JSON object and returns it.
     */
    @Override
    public void writeToJSONObject(JSONObject object)
            throws JSONException
    {
        object.put("name", mName);
        object.put("description", mDescription);
    }
}
