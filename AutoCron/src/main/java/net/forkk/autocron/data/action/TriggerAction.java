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

import android.preference.PreferenceFragment;

import net.forkk.autocron.R;
import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.State;


/**
 * Base class for "trigger" actions. Trigger actions are actions that allow the user to select
 * whether they execute their action on activate, on deactivate, or both.
 */
public abstract class TriggerAction extends ActionBase
{
    public TriggerAction(Automation parent, AutomationService service, int id)
    {
        super(parent, service, id);
    }

    public abstract void onTrigger();

    /**
     * Called when the action's automation has been activated. This should perform whatever this
     * action is meant to do on activation.
     */
    @Override
    public final void onActivate()
    {
        if (getSharedPreferences().getBoolean("trigger_activate", false)) onTrigger();
    }

    /**
     * Called when the action's automation deactivates.
     */
    @Override
    public final void onDeactivate()
    {
        if (getSharedPreferences().getBoolean("trigger_deactivate", false)) onTrigger();
    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);

        if (getParent() instanceof State)
            fragment.addPreferencesFromResource(R.xml.prefs_trigger_action);
    }
}
