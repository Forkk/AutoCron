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

package net.forkk.autocron;

import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationComponent;
import net.forkk.autocron.data.AutomationService;


public class EditComponentActivity extends FragmentActivity implements ServiceConnection
{
    public static final String EXTRA_COMPONENT_ID = "net.forkk.autocron.component_id";

    public static final String EXTRA_COMPONENT_TYPE = "net.forkk.autocron.component_type";

    public EditComponentActivity()
    {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_component);

        switch (getIntent().getIntExtra(EXTRA_COMPONENT_TYPE, -1))
        {
        case 0:
            setTitle(getResources().getString(R.string.title_activity_edit_component,
                                              getResources().getString(R.string.rule_upper)));
            break;
        case 1:
            setTitle(getResources().getString(R.string.title_activity_edit_component,
                                              getResources().getString(R.string.action_upper)));
            break;
        default:
            Log.wtf(AutomationService.LOGGER_TAG, "Invalid component type.");
            finish();
            return;
        }

        bindService(new Intent(this, AutomationService.class), this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        AutomationService.LocalBinder binder = (AutomationService.LocalBinder) iBinder;

        int automationId = getIntent().getIntExtra(EditAutomationActivity.EXTRA_AUTOMATION_ID, -1);
        int componentId = getIntent().getIntExtra(EXTRA_COMPONENT_ID, -1);

        Automation mAutomation = binder.findAutomationById(automationId);
        AutomationComponent component;

        switch (getIntent().getIntExtra(EXTRA_COMPONENT_TYPE, -1))
        {
        case 0:
            component = mAutomation.findRuleById(componentId);
            break;
        case 1:
            component = mAutomation.findActionById(componentId);
            break;
        default:
            Log.wtf(AutomationService.LOGGER_TAG, "Invalid component type.");
            finish();
            return;
        }

        ComponentPreferenceFragment fragment = new ComponentPreferenceFragment(component);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_edit_component_container, fragment);
        transaction.commit();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {

    }
}
