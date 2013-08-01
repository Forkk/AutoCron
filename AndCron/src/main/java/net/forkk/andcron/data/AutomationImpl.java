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
        implements Automation, SharedPreferences.OnSharedPreferenceChangeListener,
                           AutomationComponentBase.ComponentChangeListener
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
        edit.clear().commit();
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
        for (Rule rule : mRules)
        {
            rule.onCreate(service);
        }

        for (Action action : mActions)
        {
            action.onCreate(service);
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

        loadComponentList(context, prefs, mRuleTypeInterface);

        loadComponentList(context, prefs, mActionTypeInterface);

        onComponentListChange();

        Log.i(LOGGER_TAG, "Done loading automation configuration for \"" + getName() + "\".");
    }

    private <T extends AutomationComponent> void loadComponentList(Context context,
                                                                   SharedPreferences prefs,
                                                                   ComponentTypeInterface<T> typeInterface)
    {
        String typeName = typeInterface.getTypeName(false);

        Set<String> idSet = prefs.getStringSet(typeInterface.getIdListKey(), new HashSet<String>());

        Log.i(LOGGER_TAG, "Loading " + typeName + " list for automation \"" + getName() + "\".");

        ArrayList<T> tempComponentList = new ArrayList<T>();
        for (String stringVal : idSet)
        {
            try
            {
                int id = Integer.parseInt(stringVal);
                T component = typeInterface.loadFromPrefs(this, context, id);
                if (component != null)
                {
                    tempComponentList.add(component);
                    component.addChangeListener(this);
                    Log.i(LOGGER_TAG, "Loaded " + typeName + " \"" + component.getName() + "\".");
                }
                else
                {
                    Log.e(LOGGER_TAG, "Skipped " + typeName + " with missing or invalid type ID.");
                }
            }
            catch (NumberFormatException e)
            {
                Log.e(LOGGER_TAG, "Found non-integer in automation " + typeName + " ID set.", e);
            }
        }

        List<T> list = typeInterface.getList();
        for (T component : list)
            context.getSharedPreferences(component.getSharedPreferencesName(), Context.MODE_PRIVATE)
                   .unregisterOnSharedPreferenceChangeListener(this);
        list.clear();
        list.addAll(tempComponentList);
        for (T component : list)
            context.getSharedPreferences(component.getSharedPreferencesName(), Context.MODE_PRIVATE)
                   .registerOnSharedPreferenceChangeListener(this);
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
        addComponent(name, type, mRuleTypeInterface);
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
        deleteComponent(id, mRuleTypeInterface);
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
        addComponent(name, type, mActionTypeInterface);
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
        deleteComponent(id, mActionTypeInterface);
    }

    @Override
    public Action findActionById(int id)
    {
        for (Action action : mActions)
            if (action.getId() == id) return action;
        return null;
    }


    public <T extends AutomationComponent> void addComponent(String name, ComponentType<T> type,
                                                             ComponentTypeInterface<T> typeInterface)
    {
        SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor edit = prefs.edit();

        T component = type.createNew(this, name, getService());

        Set<String> componentIDs = new HashSet<String>();
        componentIDs.add(((Integer) component.getId()).toString());
        componentIDs
                .addAll(prefs.getStringSet(typeInterface.getIdListKey(), new HashSet<String>()));

        edit.putStringSet(typeInterface.getIdListKey(), componentIDs);
        typeInterface.getList().add(component);
        component.addChangeListener(this);
        component.onCreate(getService());
        boolean success = edit.commit();
        if (!success) Log.e(LOGGER_TAG, "Failed to commit changes to preferences.");
        else onComponentListChange();

        assert prefs.getStringSet(typeInterface.getIdListKey(), new HashSet<String>())
                    .equals(componentIDs);
    }

    public void deleteComponent(int id, ComponentTypeInterface typeInterface)
    {
        AutomationComponent component = typeInterface.findById(id);
        if (component == null)
        {
            Log.e(LOGGER_TAG, "Attempted to delete " + typeInterface.getTypeName(false) +
                              " that doesn't exist.");
            return;
        }

        SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor edit = prefs.edit();

        Set<String> componentIDs = new HashSet<String>();
        componentIDs
                .addAll(prefs.getStringSet(typeInterface.getIdListKey(), new HashSet<String>()));
        componentIDs.remove(((Integer) id).toString());

        edit.putStringSet(typeInterface.getIdListKey(), componentIDs);

        // Clear the component's preferences
        getService()
                .getSharedPreferences(component.getSharedPreferencesName(), Context.MODE_PRIVATE)
                .edit().clear().commit();

        component.onDestroy(getService());
        typeInterface.getList().remove(component);
        boolean success = edit.commit();
        if (!success) Log.e(LOGGER_TAG, "Failed to commit changes to preferences.");
        else onComponentListChange();
    }


    private final ComponentTypeInterface<Rule> mRuleTypeInterface =
            new ComponentTypeInterface<Rule>()
            {
                @Override
                public String getTypeName(boolean upper)
                {
                    return upper ? "Rule" : "rule";
                }

                @Override
                public String getIdListKey()
                {
                    return VALUE_RULE_IDS;
                }

                @Override
                public List<Rule> getList()
                {
                    return mRules;
                }

                @Override
                public Rule loadFromPrefs(Automation automation, Context context, int id)
                {
                    return RuleType.fromSharedPreferences(automation, context, id);
                }

                @Override
                public Rule findById(int id)
                {
                    return findRuleById(id);
                }
            };

    private final ComponentTypeInterface<Action> mActionTypeInterface =
            new ComponentTypeInterface<Action>()
            {
                @Override
                public String getTypeName(boolean upper)
                {
                    return upper ? "Action" : "action";
                }

                @Override
                public String getIdListKey()
                {
                    return VALUE_ACTION_IDS;
                }

                @Override
                public List<Action> getList()
                {
                    return mActions;
                }

                @Override
                public Action loadFromPrefs(Automation automation, Context context, int id)
                {
                    return ActionType.fromSharedPreferences(automation, context, id);
                }

                @Override
                public Action findById(int id)
                {
                    return findActionById(id);
                }
            };

    @Override
    public void onComponentChange()
    {
        onComponentListChange();
    }

    private interface ComponentTypeInterface<T extends AutomationComponent>
    {
        public abstract String getTypeName(boolean upper);

        public abstract String getIdListKey();

        public abstract List<T> getList();

        public abstract T loadFromPrefs(Automation automation, Context context, int id);

        public abstract T findById(int id);
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
     * Reloads all components from configuration.
     * <p/>
     * This is generally called when some configuration options change. It re-creates all of the
     * rules and actions.
     */
    @Override
    public void reloadComponents(AutomationService service)
    {
        for (Rule rule : mRules)
            rule.onDestroy(service);

        for (Action action : mActions)
            action.onDestroy(service);

        loadConfig(service);

        for (Rule rule : mRules)
            rule.onCreate(service);

        for (Action action : mActions)
            action.onCreate(service);
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
    protected SharedPreferences getSharedPreferences()
    {
        return getService().getSharedPreferences(getSharedPreferencesName(), Context.MODE_PRIVATE);
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
