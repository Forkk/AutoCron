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

import android.util.Log;

import net.forkk.andcron.data.action.Action;
import net.forkk.andcron.data.action.ActionLoader;
import net.forkk.andcron.data.rule.Rule;
import net.forkk.andcron.data.rule.RuleLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Standard implementation for the automation interface.
 */
public class AutomationImpl implements Automation
{
    public static final String LOGGER_TAG = AutomationService.LOGGER_TAG;

    private String mName;

    private String mDescription;

    private ArrayList<Rule> mRules;

    private ArrayList<Action> mActions;

    public static Automation fromJSONObject(JSONObject object)
            throws JSONException
    {
        AutomationImpl automation = new AutomationImpl();
        automation.readFromJSONObject(object);
        return automation;
    }

    public AutomationImpl()
    {
        mRules = new ArrayList<Rule>();
        mActions = new ArrayList<Action>();
    }

    /**
     * @return An array of this automation's rules.
     */
    @Override
    public Rule[] getRules()
    {
        return mRules.toArray(new Rule[mRules.size()]);
    }

    /**
     * @return An array of this automation's actions.
     */
    @Override
    public Action[] getActions()
    {
        return mActions.toArray(new Action[mActions.size()]);
    }

    /**
     * @return The user-given name for this component.
     */
    @Override
    public String getName()
    {
        return null;
    }

    /**
     * @return The user-given description for this component.
     */
    @Override
    public String getDescription()
    {
        return null;
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
        Log.i(LOGGER_TAG, "Loading automation from JSON object.");

        Log.i(LOGGER_TAG, "Loading metadata.");
        mName = object.getString("name");
        mDescription = object.getString("description");

        Log.i(LOGGER_TAG, "Loading rules.");
        ArrayList<Rule> tempRuleList = new ArrayList<Rule>();
        JSONArray rules = object.getJSONArray("rules");
        for (int i = 0; i < rules.length(); i++)
        {
            Rule rule = RuleLoader.loadFromJSON(rules.getJSONObject(i));
            tempRuleList.add(rule);
        }
        mRules.clear();
        mRules.addAll(tempRuleList);

        Log.i(LOGGER_TAG, "Loading actions.");
        ArrayList<Action> tempActionList = new ArrayList<Action>();
        JSONArray actions = object.getJSONArray("actions");
        for (int i = 0; i < actions.length(); i++)
        {
            Action action = ActionLoader.loadFromJSON(actions.getJSONObject(i));
            tempActionList.add(action);
        }
        mActions.clear();
        mActions.addAll(tempActionList);

        Log.i(LOGGER_TAG, "Done loading automation " + getName() + ".");
    }

    /**
     * Writes the component's settings to a JSON object and returns it.
     */
    @Override
    public void writeToJSONObject(JSONObject object)
    throws JSONException
    {
        Log.i(LOGGER_TAG, "Saving automation " + getName() + ".");

        Log.i(LOGGER_TAG, "Saving metadata.");
        object.put("name", mName);
        object.put("description", mDescription);

        Log.i(LOGGER_TAG, "Saving rules.");
        JSONArray rules = new JSONArray();
        for (Rule rule : mRules)
        {
            JSONObject ruleObj = new JSONObject();
            rule.writeToJSONObject(ruleObj);
            rules.put(ruleObj);
        }
        object.put("rules", rules);

        Log.i(LOGGER_TAG, "Saving actions.");
        JSONArray actions = new JSONArray();
        for (Action action : mActions)
        {
            JSONObject actionObj = new JSONObject();
            action.writeToJSONObject(actionObj);
            actions.put(actionObj);
        }
        object.put("actions", actions);

        Log.i(LOGGER_TAG, "Done saving automation " + getName() + ".");
    }
}
