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

package net.forkk.autocron.data.rule;

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
 * Custom rule whose state can be changed by an action.
 */
public class CustomRule extends RuleBase implements CustomRuleService.CustomRuleListener
{
    private static final String VALUE_RULE_ID = "custom_rule_id";

    private static RuleType sComponentType;

    public static RuleType initComponentType(Resources res)
    {
        return sComponentType = new RuleType(res.getString(R.string.custom_rule_title),
                                             res.getString(R.string.custom_rule_description),
                                             CustomRule.class);
    }

    public static RuleType getComponentType()
    {
        return sComponentType;
    }

    public CustomRule(Automation parent, AutomationService service, int id)
    {
        super(parent, service, id);
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

    /**
     * Called after the automation service finishes loading components. This should perform all
     * necessary initialization for this component.
     */
    @Override
    protected void onCreate()
    {
        final AutomationService service = getService();
        assert service != null;

        SharedPreferences prefs = getSharedPreferences();
        if (prefs.getString(VALUE_RULE_ID, null) == null)
            prefs.edit().putString(VALUE_RULE_ID, Integer.toString(getId()));

        registerListener();
    }

    /**
     * Called when the automation service is destroyed. This should perform all necessary cleanup.
     */
    @Override
    protected void onDestroy()
    {
        unregisterListener();
    }


    @Override
    public void onCustomRuleStateChange(String id, boolean state)
    {
        if (id.equals(getListenerId())) setActive(state);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
    {
        super.onSharedPreferenceChanged(preferences, key);
        if (key.equals(VALUE_RULE_ID))
        {
            unregisterListener();
            registerListener();
        }
    }


    protected String getListenerId()
    {
        return getSharedPreferences().getString(VALUE_RULE_ID, Integer.toString(getId()));
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
                binder.registerCustomRuleListener(getListenerId(), CustomRule.this);

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
                binder.unregisterCustomRuleListener(getListenerId(), CustomRule.this);

                service.unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName)
            {

            }
        }, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_custom_rule);
    }
}
