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

package net.forkk.andcron.data.action;

import android.content.Context;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import net.forkk.andcron.R;
import net.forkk.andcron.data.Automation;
import net.forkk.andcron.data.AutomationService;


/**
 * Test action that shows a toast when activated or deactivated.
 */
public class TestAction extends ActionBase
{
    private Context mContext;

    public TestAction(Automation parent, Context context, int id)
    {
        super(parent, context, id);
    }

    /**
     * Called when the action's automation has been activated. This should perform whatever this
     * action is meant to do on activation.
     */
    @Override
    public void onActivate(AutomationService service)
    {
        Toast.makeText(service, getActivationMessage(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the action's automation deactivates.
     */
    @Override
    public void onDeactivate(AutomationService service)
    {
        Toast.makeText(service, getDeactivationMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(AutomationService service)
    {
        mContext = service;
    }

    @Override
    public void onDestroy(AutomationService service)
    {
        mContext = null;
    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_test_action);
    }

    protected String getActivationMessage()
    {
        return getSharedPreferences().getString("activate_message",
                                                mContext.getString(R.string.pref_default_test_action_activate));
    }

    protected String getDeactivationMessage()
    {
        return getSharedPreferences().getString("deactivate_message",
                                                mContext.getString(R.string.pref_default_test_action_deactivate));
    }
}
