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
 * Action that changes the state of a custom rule.
 */
public class CustomRuleAction extends TriggerAction
{
    private static final String VALUE_RULE_ID = "custom_rule_id";

    private static final String VALUE_STATE_CHANGE_TYPE = "state_change_type";

    private static ActionType sComponentType;

    public static ActionType initComponentType(Resources res)
    {
        return sComponentType = new ActionType(res.getString(R.string.custom_rule_action_title),
                                               res.getString(R.string.custom_rule_action_description),
                                               CustomRuleAction.class);
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


    public CustomRuleAction(Automation parent, AutomationService service, int id)
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
                String id = getListenerId();
                switch (getStateChangeType())
                {
                case Activate:
                    binder.setState(id, true);
                    break;

                case Deactivate:
                    binder.setState(id, false);
                    break;

                case Toggle:
                    binder.setState(id, !binder.getState(id));
                    break;
                }
                service.unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName)
            {

            }
        }, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onCreate()
    {

    }

    @Override
    protected void onDestroy()
    {

    }


    protected String getListenerId()
    {
        return getSharedPreferences().getString(VALUE_RULE_ID, Integer.toString(getId()));
    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_custom_rule_action);
    }

    protected StateChangeType getStateChangeType()
    {
        String typeString = getSharedPreferences().getString(VALUE_STATE_CHANGE_TYPE, "none");

        if (typeString.equals("activate")) return StateChangeType.Activate;
        else if (typeString.equals("deactivate")) return StateChangeType.Deactivate;
        else if (typeString.equals("toggle")) return StateChangeType.Toggle;
        return StateChangeType.None;
    }

    protected enum StateChangeType
    {
        Activate,
        Deactivate,
        Toggle,
        None,
    }
}
