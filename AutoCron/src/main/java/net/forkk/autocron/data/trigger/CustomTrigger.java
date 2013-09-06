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

package net.forkk.autocron.data.trigger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.IBinder;
import android.preference.PreferenceFragment;

import net.forkk.autocron.R;
import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentType;
import net.forkk.autocron.data.CustomRuleService;


/**
 * A custom trigger that can be triggered by an action.
 */
public class CustomTrigger extends TriggerBase implements CustomRuleService.CustomTriggerListener
{
    private static final String VALUE_CUSTOM_TRIGGER_ID = "custom_trigger_id";

    private static TriggerType sComponentType;

    public static TriggerType initComponentType(Resources res)
    {
        return sComponentType = new TriggerType(res.getString(R.string.custom_trigger_title),
                                                res.getString(R.string.custom_trigger_description),
                                                CustomTrigger.class);
    }

    public static TriggerType getComponentType()
    {
        return sComponentType;
    }

    @Override
    public ComponentType getType()
    {
        return getComponentType();
    }


    public CustomTrigger(Automation parent, AutomationService service, int id)
    {
        super(parent, service, id);
    }


    @Override
    protected void onCreate()
    {
        registerListener();
    }

    @Override
    protected void onDestroy()
    {
        unregisterListener();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
    {
        super.onSharedPreferenceChanged(preferences, key);

        if (key.equals(VALUE_CUSTOM_TRIGGER_ID))
        {
            unregisterListener();
            registerListener();
        }
    }


    protected void registerListener()
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
                binder.registerCustomTriggerListener(getCustomTriggerId(), CustomTrigger.this);

                service.unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName)
            {

            }
        }, Context.BIND_AUTO_CREATE);
    }

    protected void unregisterListener()
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
                binder.unregisterCustomTriggerListener(getCustomTriggerId(), CustomTrigger.this);

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

    /**
     * Called when a custom trigger that ID that this listener is registered to is triggered.
     *
     * @param id
     *         The ID that was triggered.
     */
    @Override
    public void onCustomTriggerTriggered(String id)
    {
        if (id.equals(getCustomTriggerId())) trigger();
    }


    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_custom_trigger);
    }
}
