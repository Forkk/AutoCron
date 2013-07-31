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

import net.forkk.andcron.R;
import net.forkk.andcron.data.action.Action;
import net.forkk.andcron.data.rule.Rule;

import java.util.ArrayList;


/**
 * Standard implementation for the automation interface.
 */
public class AutomationImpl extends ConfigComponentBase implements Automation
{
    private ArrayList<Rule> mRules;

    private ArrayList<Action> mActions;

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
        mRules = new ArrayList<Rule>();
        mActions = new ArrayList<Action>();
        mIsActive = false;
    }

    /**
     * Called after the automation service finishes loading components. This should perform all
     * necessary initialization for this component.
     *
     * @param context
     *         Context to initialize with.
     */
    @Override
    public void onCreate(Context context)
    {
        for (Action action : mActions)
        {
            action.onCreate(context);
        }

        for (Rule rule : mRules)
        {
            rule.onCreate(context);
        }
    }

    /**
     * Called when the automation service is destroyed. This should perform all necessary cleanup.
     */
    @Override
    public void onDestroy()
    {
        for (Rule rule : mRules)
        {
            rule.onDestroy();
        }

        for (Action action : mActions)
        {
            action.onDestroy();
        }
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
                    action.onActivate();
            }
            else
            {
                for (Action action : mActions)
                    action.onDeactivate();
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
}
