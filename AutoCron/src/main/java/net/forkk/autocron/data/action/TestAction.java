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

package net.forkk.autocron.data.action;

import android.content.res.Resources;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import net.forkk.autocron.R;
import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentType;


/**
 * Test action that shows a toast when activated or deactivated.
 */
public class TestAction extends ActionBase
{
    private static ActionType sComponentType;

    public static ActionType initComponentType(Resources res)
    {
        return sComponentType = new ActionType(res.getString(R.string.test_action_title),
                                               res.getString(R.string.test_action_description),
                                               TestAction.class);
    }

    public static ActionType getComponentType()
    {
        return sComponentType;
    }

    public TestAction(Automation parent, AutomationService service, int id)
    {
        super(parent, service, id);
    }

    /**
     * Called when the action's automation has been activated. This should perform whatever this
     * action is meant to do on activation.
     */
    @Override
    public void onActivate()
    {
        Toast.makeText(getService(), getActivationMessage(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the action's automation deactivates.
     */
    @Override
    public void onDeactivate()
    {
        Toast.makeText(getService(), getDeactivationMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate()
    {

    }

    @Override
    public void onDestroy()
    {

    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_test_action);
    }

    protected String getActivationMessage()
    {
        return getSharedPreferences().getString("activate_message", getService()
                                                                            .getString(R.string.pref_default_test_action_activate));
    }

    protected String getDeactivationMessage()
    {
        return getSharedPreferences().getString("deactivate_message", getService()
                                                                              .getString(R.string.pref_default_test_action_deactivate));
    }

    /**
     * Gets this automation's component type. This should return the same object for all components
     * of this type.
     *
     * @return The component type object for this component.
     */
    @Override
    public ComponentType getType()
    {
        return getComponentType();
    }
}
