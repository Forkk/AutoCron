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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.IBinder;
import android.preference.PreferenceFragment;

import net.forkk.autocron.R;
import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentType;
import net.forkk.autocron.data.CustomRuleService;


/**
 * An action that triggers a custom trigger.
 */
public class CustomTriggerAction extends TriggerAction
{
    private static final String VALUE_CUSTOM_TRIGGER_ID = "custom_trigger_id";

    private static ActionType sComponentType;

    public static ActionType initComponentType(Resources res)
    {
        return sComponentType = new ActionType(res.getString(R.string.custom_trigger_action_title),
                                               res.getString(R.string.custom_trigger_action_description),
                                               CustomTriggerAction.class);
    }

    public static ActionType getComponentType()
    {
        return sComponentType;
    }

    @Override
    public ComponentType getType()
    {
        return getComponentType();
    }


    public CustomTriggerAction(Automation parent, AutomationService service, int id)
    {
        super(parent, service, id);
    }


    @Override
    public void onTrigger()
    {
        final AutomationService service = getService();
        assert service != null;

        Intent serviceIntent = new Intent(service, CustomRuleService.class);
        service.bindService(serviceIntent, new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder)
            {
                CustomRuleService.LocalBinder binder = (CustomRuleService.LocalBinder) iBinder;
                binder.triggerCustomTrigger(getCustomTriggerId());
                service.unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName)
            {

            }
        }, Context.BIND_AUTO_CREATE);
    }


    protected String getCustomTriggerId()
    {
        return getSharedPreferences().getString(VALUE_CUSTOM_TRIGGER_ID, "");
    }


    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_custom_trigger_action);
    }

    @Override
    protected void onCreate()
    {

    }

    @Override
    protected void onDestroy()
    {

    }
}
