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
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;

import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentPointer;
import net.forkk.autocron.data.ConfigComponent;
import net.forkk.autocron.data.Event;
import net.forkk.autocron.data.State;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment that shows a list of automations.
 */
public class AutomationListFragment extends ComponentListFragment
        implements ServiceConnection, AutomationService.AutomationListChangeListener
{
    private static final String VALUE_AUTOMATION_TYPE = "net.forkk.autocron.automation_type";

    private AutomationService.LocalBinder mBinder;

    private AutomationListType mType;

    public enum AutomationListType
    {
        State,
        Event
    }

    @SuppressWarnings("UnusedDeclaration")
    public AutomationListFragment()
    {
        // Obligatory empty constructor.
    }

    public AutomationListFragment(AutomationListType type)
    {
        Bundle arguments = new Bundle();
        arguments.putSerializable(VALUE_AUTOMATION_TYPE, type);
        setArguments(arguments);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        assert arguments != null;
        mType = (AutomationListType) arguments.getSerializable(VALUE_AUTOMATION_TYPE);

        Activity activity = getActivity();
        assert activity != null;

        Intent intent = new Intent(getActivity(), AutomationService.class);
        activity.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (mBinder != null) mBinder.unregisterAutomationListChangeListener(this);

        Activity activity = getActivity();
        assert activity != null;
        activity.unbindService(this);
    }

    @Override
    protected void onEditComponent(long id)
    {
        Intent intent = new Intent(getActivity(), EditAutomationActivity.class);

        ComponentPointer pointer = null;

        switch (mType)
        {
        case State:
            pointer = new State.Pointer((int) id);
            break;
        case Event:
            pointer = new Event.Pointer((int) id);
            break;
        }

        intent.putExtra(EditAutomationActivity.EXTRA_AUTOMATION_POINTER, pointer);
        startActivity(intent);
    }

    @Override
    protected void onDeleteComponent(int id)
    {
        switch (mType)
        {
        case State:
            mBinder.deleteState(id);
            break;
        case Event:
            mBinder.deleteEvent(id);
        }
    }

    @Override
    protected void onActionAddComponent()
    {
        Activity activity = getActivity();
        assert activity != null;

        final View inputView = activity.getLayoutInflater().inflate(R.layout.text_entry_view, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getResources().getString(R.string.title_new_component,
                                                  getComponentTypeName(true)));
        builder.setMessage(getResources().getString(R.string.message_new_component,
                                                    getComponentTypeName(false)));
        builder.setView(inputView);
        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.dismiss();
                assert inputView != null;
                EditText input = (EditText) inputView.findViewById(R.id.text_input);
                //noinspection ConstantConditions
                final String name = input.getText().toString();

                // Add a new component with the given name.
                switch (mType)
                {
                case State:
                    mBinder.createNewState(name);
                    break;
                case Event:
                    mBinder.createNewEvent(name);
                    break;
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public String getComponentTypeName(boolean upper)
    {
        switch (mType)
        {
        case State:
            return getResources().getString(upper ? R.string.state_upper : R.string.state_lower);

        case Event:
            return getResources().getString(upper ? R.string.event_upper : R.string.event_lower);
        }

        return getResources()
                       .getString(upper ? R.string.automation_upper : R.string.automation_lower);
    }

    @Override
    protected List<ConfigComponent> getComponentList()
    {
        switch (mType)
        {
        case State:
            return new ArrayList<ConfigComponent>(mBinder.getStateList());
        case Event:
            return new ArrayList<ConfigComponent>(mBinder.getEventList());
        }
        return null;
    }

    @Override
    protected boolean hasItems()
    {
        return mBinder != null;
    }

    /**
     * Finds the component with the given ID.
     */
    @Override
    protected ConfigComponent findComponentById(int id)
    {
        if (mBinder == null) return null;

        switch (mType)
        {
        case State:
            return mBinder.findStateById(id);
        case Event:
            return mBinder.findEventById(id);
        }

        return null;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        mBinder = (AutomationService.LocalBinder) iBinder;
        mBinder.registerAutomationListChangeListener(this);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {
        mBinder = null;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAutomationListChange()
    {
        mAdapter.notifyDataSetChanged();
    }
}
