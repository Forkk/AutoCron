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

import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ConfigComponent;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment that shows a list of automations.
 */
public class AutomationListFragment extends ComponentListFragment
        implements ServiceConnection, AutomationService.AutomationListChangeListener
{
    private AutomationService.LocalBinder mBinder;

    public AutomationListFragment()
    {
        // Obligatory empty constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(getActivity(), AutomationService.class);
        getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (mBinder != null) mBinder.unregisterAutomationListChangeListener(this);
        getActivity().unbindService(this);
    }

    @Override
    protected void onEditComponent(long id)
    {
        Intent intent = new Intent(getActivity(), EditAutomationActivity.class);
        intent.putExtra(EditAutomationActivity.EXTRA_AUTOMATION_POINTER,
                        new Automation.Pointer((int) id));
        startActivity(intent);
    }

    @Override
    protected void onDeleteComponent(int id)
    {
        mBinder.deleteAutomation(id);
    }

    @Override
    protected void onActionAddComponent()
    {
        final View inputView =
                getActivity().getLayoutInflater().inflate(R.layout.text_entry_view, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                mBinder.createNewAutomation(name);
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
        if (upper) return getResources().getString(R.string.automation_upper);
        else return getResources().getString(R.string.automation_lower);
    }

    @Override
    protected List<ConfigComponent> getComponentList()
    {
        return new ArrayList<ConfigComponent>(mBinder.getAutomationList());
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
        return mBinder.findAutomationById(id);
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
