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

package net.forkk.autocron.data;

import android.content.SharedPreferences;

import net.forkk.autocron.data.action.Action;
import net.forkk.autocron.data.rule.Rule;


/**
 * Standard implementation for the state interface.
 */
public class StateBase extends AutomationBase
        implements State, SharedPreferences.OnSharedPreferenceChangeListener,
                           AutomationComponentBase.ComponentChangeListener
{
    public static final String LOGGER_TAG = AutomationService.LOGGER_TAG;

    private boolean mIsActive;

    /**
     * Loads a new state from the given SharedPreferences.
     *
     * @param service
     *         The automation service.
     * @param preferencesId
     *         The ID of the shared preferences to load from.
     *
     * @return The new state.
     */
    public static State fromSharedPreferences(AutomationService service, int preferencesId)
    {
        return new StateBase(service, preferencesId);
    }

    public static State createNewState(String name, AutomationService service, int preferencesId)
    {
        StateBase state = new StateBase(service, preferencesId);
        SharedPreferences.Editor edit = state.getSharedPreferences().edit();
        edit.clear().commit();
        edit.putString(VALUE_NAME, name);
        edit.commit();
        return state;
    }

    public StateBase(AutomationService service, int sharedPreferencesId)
    {
        super(service, sharedPreferencesId);
        mIsActive = false;
        loadConfig(service);
    }

    /**
     * @return True or false depending on whether or not the state is active.
     */
    @Override
    public boolean isActive()
    {
        return mIsActive;
    }

    /**
     * Called when a rule is activated or deactivated. This should recheck all of the rules and see
     * if the state should be activated.
     */
    @Override
    public void updateActivationState()
    {
        if (!isEnabled()) return;

        boolean activated = true;
        int enabledRuleCount = 0;
        for (Rule rule : mRules)
        {
            if (rule.isEnabled())
            {
                enabledRuleCount++;

                // If the rule is inverted and active or the rule is not inverted and inactive, 
                // consider the rule inactive.
                if (rule.isInverted() == rule.isActive()) activated = false;
            }
        }

        // If there are no enabled rules, the state does not activate.
        if (enabledRuleCount <= 0) activated = false;

        if (mIsActive != activated)
        {
            mIsActive = activated;
            if (mIsActive)
            {
                for (Action action : mActions)
                    if (action.isEnabled()) action.onActivate();
            }
            else
            {
                for (Action action : mActions)
                    if (action.isEnabled()) action.onDeactivate();
            }
        }
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
        return "state_" + id;
    }

    @Override
    public ComponentPointer getPointer()
    {
        return new Pointer(this);
    }

    @Override
    public boolean isComponentTypeCompatible(ComponentType<? extends AutomationComponent> type)
    {
        // All rule and action types are compatible with state automations.
        Class<? extends AutomationComponent> componentClass = type.getTypeClass();
        return Action.class.isAssignableFrom(componentClass) ||
               Rule.class.isAssignableFrom(componentClass);
    }
}
