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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceFragment;

import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentPointer;
import net.forkk.autocron.data.ConfigComponent;


/**
 * PreferenceFragment for editing a component's preferences.
 */
public class ComponentPreferenceFragment extends PreferenceFragment
{
    public static final String VALUE_COMPONENT_POINTER = "net.forkk.autocron.component_pointer";

    private ConfigComponent mComponent;

    private ComponentPointer mPointer;

    public ComponentPreferenceFragment()
    {
        super();
    }

    public ComponentPreferenceFragment(ComponentPointer pointer)
    {
        Bundle arguments = new Bundle();
        arguments.putSerializable(VALUE_COMPONENT_POINTER, pointer);
        setArguments(arguments);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        assert arguments != null;
        mPointer = (ComponentPointer) arguments.getSerializable(VALUE_COMPONENT_POINTER);
        assert mPointer != null;
        loadComponent();
    }

    public void loadComponent()
    {
        assert mPointer != null;
        ServiceConnection connection = new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder)
            {
                AutomationService.LocalBinder binder = (AutomationService.LocalBinder) iBinder;
                mComponent = mPointer.getComponent(binder);
                assert mComponent != null;
                initFromComponent();

                Activity activity = getActivity();
                assert activity != null;
                activity.unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName)
            {

            }
        };

        Activity activity = getActivity();
        assert activity != null;

        Intent service = new Intent(activity, AutomationService.class);
        activity.bindService(service, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putSerializable(VALUE_COMPONENT_POINTER, mComponent.getPointer());
    }

    public void initFromComponent()
    {
        //noinspection ConstantConditions
        getPreferenceManager().setSharedPreferencesName(mComponent.getSharedPreferencesName());

        // Load preferences from the component.
        mComponent.addPreferencesToFragment(this);
    }
}
