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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceFragment;
import android.util.Log;

import net.forkk.andcron.R;
import net.forkk.andcron.data.action.Action;
import net.forkk.andcron.data.action.ActionType;
import net.forkk.andcron.data.rule.Rule;
import net.forkk.andcron.data.rule.RuleType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Standard implementation for the automation interface.
 */
public class AutomationImpl extends ConfigComponentBase
        implements Automation, SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String LOGGER_TAG = AutomationService.LOGGER_TAG;

    private static final String VALUE_RULE_IDS = "rule_ids";

    private static final String VALUE_ACTION_IDS = "action_ids";

    private ArrayList<Rule> mRules;

    private ArrayList<Action> mActions;

    private ArrayList<ComponentListChangeListener> mComponentListObservers;

    private AutomationService mAutomationService;

    private boolean mIsActive;

    /**
     * Loads a new automation from the given SharedPreferences.
     *
     * @param service
     *         The automation service.
     * @param preferencesId
     *         The ID of the shared preferences to load from.
     *
     * @return The new automation.
     */
    public static Automation fromSharedPreferences(AutomationService service, int preferencesId)
    {
        return new AutomationImpl(service, preferencesId);
    }

    public static Automation createNewAutomation(String name, AutomationService service,
                                                 int preferencesId)
    {
        AutomationImpl automation = new AutomationImpl(service, preferencesId);
        SharedPreferences.Editor edit = automation.getSharedPreferences().edit();
        edit.clear();
        edit.putString(VALUE_NAME, name);
        edit.commit();
        return automation;
    }

    public AutomationImpl(AutomationService service, int sharedPreferencesId)
    {
        super(service, sharedPreferencesId);
        mAutomationService = service;
        mComponentListObservers = new ArrayList<ComponentListChangeListener>();
        mRules = new ArrayList<Rule>();
        mActions = new ArrayList<Action>();
        mIsActive = false;
        loadConfig(service);
    }

    /**
     * Called after the automation service finishes loading components. This should perform all
     * necessary initialization for this component.
     *
     * @param service
     *         Context to initialize with.
     */
    @Override
    public void onCreate(AutomationService service)
    {
        for (Action action : mActions)
        {
            action.onCreate(service);
        }

        for (Rule rule : mRules)
        {
            rule.onCreate(service);
        }
    }

    /**
     * Called when the automation service is destroyed. This should perform all necessary cleanup.
     */
    @Override
    public void onDestroy(AutomationService service)
    {
        for (Rule rule : mRules)
        {
            rule.onDestroy(service);
        }

        for (Action action : mActions)
        {
            action.onDestroy(service);
        }
    }

    protected void loadConfig(Context context)
    {
        Log.i(LOGGER_TAG, "Loading automation configuration for \"" + getName() + "\".");

        SharedPreferences prefs = getSharedPreferences();
        Set<String> ruleIDs = prefs.getStringSet(VALUE_RULE_IDS, new HashSet<String>());

        Log.i(LOGGER_TAG, "Loading rule list for automation \"" + getName() + "\".");

        ArrayList<Rule> tempRuleList = new ArrayList<Rule>();
        for (String stringVal : ruleIDs)
        {
            try
            {
                int id = Integer.parseInt(stringVal);
                Rule rule = RuleType.fromSharedPreferences(this, context, id);
                if (rule != null)
                {
                    tempRuleList.add(rule);
                    Log.i(LOGGER_TAG, "Loaded rule \"" + rule.getName() + "\".");
                }
                else
                {
                    Log.w(LOGGER_TAG, "Skipped rule with missing or invalid type ID.");
                }
            }
            catch (NumberFormatException e)
            {
                Log.e(LOGGER_TAG, "Found non-integer in automation ID set.", e);
            }
        }

        for (Rule rule : mRules)
            context.getSharedPreferences(rule.getSharedPreferencesName(), Context.MODE_PRIVATE)
                   .unregisterOnSharedPreferenceChangeListener(this);
        mRules.clear();
        mRules.addAll(tempRuleList);
        for (Rule rule : mRules)
            context.getSharedPreferences(rule.getSharedPreferencesName(), Context.MODE_PRIVATE)
                   .registerOnSharedPreferenceChangeListener(this);

        Log.i(LOGGER_TAG, "Loading action list for automation \"" + getName() + "\".");

        ArrayList<Action> tempActionList = new ArrayList<Action>();
        for (String stringVal : ruleIDs)
        {
            try
            {
                int id = Integer.parseInt(stringVal);
                Action action = ActionType.fromSharedPreferences(this, context, id);
                if (action != null)
                {
                    tempActionList.add(action);
                    Log.i(LOGGER_TAG, "Loaded action \"" + action.getName() + "\".");
                }
                else
                {
                    Log.w(LOGGER_TAG, "Skipped action with missing or invalid type ID.");
                }
            }
            catch (NumberFormatException e)
            {
                Log.e(LOGGER_TAG, "Found non-integer in automation ID set.", e);
            }
        }

        for (Action action : mActions)
            context.getSharedPreferences(action.getSharedPreferencesName(), Context.MODE_PRIVATE)
                   .unregisterOnSharedPreferenceChangeListener(this);
        mActions.clear();
        mActions.addAll(tempActionList);
        for (Action action : mActions)
            context.getSharedPreferences(action.getSharedPreferencesName(), Context.MODE_PRIVATE)
                   .registerOnSharedPreferenceChangeListener(this);

        Log.i(LOGGER_TAG, "Done loading automation configuration for \"" + getName() + "\".");
    }

    /**
     * @return An array of this automation's rules.
     */
    @Override
    public List<Rule> getRules()
    {
        return mRules;
    }

    /**
     * Adds a new rule of the given type with the given name.
     *
     * @param name
     *         Name of the rule to add.
     * @param type
     *         The type of rule to add.
     */
    @Override
    public void addRule(String name, RuleType type)
    {
        SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor edit = prefs.edit();

        Rule rule = type.createNew(this, name, getService());

        Set<String> componentIDs = new HashSet<String>();
        componentIDs.add(((Integer) rule.getId()).toString());
        componentIDs.addAll(prefs.getStringSet(VALUE_RULE_IDS, new HashSet<String>()));

        edit.putStringSet(VALUE_RULE_IDS, componentIDs);
        mRules.add(rule);
        boolean success = edit.commit();
        if (!success) Log.e(LOGGER_TAG, "Failed to commit changes to preferences.");
        else onComponentListChange();

        assert prefs.getStringSet(VALUE_RULE_IDS, new HashSet<String>()).equals(componentIDs);
    }

    /**
     * Removes the rule with the given ID.
     *
     * @param id
     *         The ID of the rule to remove.
     */
    @Override
    public void deleteRule(int id)
    {
        Rule rule = findRuleById(id);
        if (rule == null)
        {
            Log.e(LOGGER_TAG, "Attempted to delete a rule that doesn't exist.");
            return;
        }

        SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor edit = prefs.edit();

        Set<String> ruleIDs = new HashSet<String>();
        ruleIDs.addAll(prefs.getStringSet(VALUE_RULE_IDS, new HashSet<String>()));
        ruleIDs.remove(((Integer) id).toString());

        edit.putStringSet(VALUE_RULE_IDS, ruleIDs);
        mRules.remove(rule);
        boolean success = edit.commit();
        if (!success) Log.e(LOGGER_TAG, "Failed to commit changes to preferences.");
        else onComponentListChange();
    }

    @Override
    public Rule findRuleById(int id)
    {
        for (Rule rule : mRules)
            if (rule.getId() == id) return rule;
        return null;
    }

    /**
     * @return An array of this automation's actions.
     */
    @Override
    public List<Action> getActions()
    {
        return mActions;
    }

    /**
     * Adds a new action with the given name.
     *
     * @param name
     *         Name of the action to add.
     * @param type
     *         The type of action to add.
     */
    @Override
    public void addAction(String name, ActionType type)
    {
        SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor edit = prefs.edit();

        Action action = type.createNew(this, name, getService());

        Set<String> componentIDs = new HashSet<String>();
        componentIDs.add(((Integer) action.getId()).toString());
        componentIDs.addAll(prefs.getStringSet(VALUE_ACTION_IDS, new HashSet<String>()));

        edit.putStringSet(VALUE_ACTION_IDS, componentIDs);
        mActions.add(action);
        boolean success = edit.commit();
        if (!success) Log.e(LOGGER_TAG, "Failed to commit changes to preferences.");
        else onComponentListChange();

        assert prefs.getStringSet(VALUE_ACTION_IDS, new HashSet<String>()).equals(componentIDs);
    }

    /**
     * Removes the action with the given ID.
     *
     * @param id
     *         The ID of the action to remove.
     */
    @Override
    public void deleteAction(int id)
    {
        Action action = findActionById(id);
        if (action == null)
        {
            Log.e(LOGGER_TAG, "Attempted to delete an action that doesn't exist.");
            return;
        }

        SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor edit = prefs.edit();

        Set<String> actionIDs = new HashSet<String>();
        actionIDs.addAll(prefs.getStringSet(VALUE_ACTION_IDS, new HashSet<String>()));
        actionIDs.remove(((Integer) id).toString());

        edit.putStringSet(VALUE_ACTION_IDS, actionIDs);
        mActions.remove(action);
        boolean success = edit.commit();
        if (!success) Log.e(LOGGER_TAG, "Failed to commit changes to preferences.");
        else onComponentListChange();
    }

    @Override
    public Action findActionById(int id)
    {
        for (Action action : mActions)
            if (action.getId() == id) return action;
        return null;
    }

    /**
     * @return True or false depending on whether or not the automation is active.
     */
    @Override
    public boolean isActive()
    {
        return mIsActive;
    }

    /**
     * Called when a rule is activated or deactivated. This should recheck all of the rules and see
     * if the automation should be activated.
     */
    @Override
    public void updateActivationState()
    {
        boolean activated = true;
        for (Rule rule : mRules)
            if (!rule.isActive()) activated = false;

        if (mIsActive != activated)
        {
            mIsActive = activated;
            if (mIsActive)
            {
                for (Action action : mActions)
                    action.onActivate(getService());
            }
            else
            {
                for (Action action : mActions)
                    action.onDeactivate(getService());
            }
        }
    }

    /**
     * @return The automation service that this automation is attached to.
     */
    @Override
    public AutomationService getService()
    {
        return mAutomationService;
    }

    /**
     * Gets this component's shared preferences name for the given component ID.
     *
     * @param id
     *         The component's ID.
     *
     * @return The shared preferences ID for the corresponding ID.
     */
    @Override
    protected String getSharedPreferencesName(int id)
    {
        return "automation_" + id;
    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_automation);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
    {
        if (key.equals("name") || key.equals("description")) onComponentListChange();
    }

    @Override
    public void registerComponentListObserver(ComponentListChangeListener listener)
    {
        mComponentListObservers.add(listener);
    }

    @Override
    public void unregisterComponentListObserver(ComponentListChangeListener listener)
    {
        mComponentListObservers.remove(listener);
    }

    private void onComponentListChange()
    {
        for (ComponentListChangeListener listener : mComponentListObservers)
            listener.onComponentListChange();
    }

    public static interface ComponentListChangeListener
    {
        public void onComponentListChange();
    }
}
