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

package net.forkk.andcron;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import net.forkk.andcron.data.Automation;
import net.forkk.andcron.data.AutomationService;


/**
 * Fragment that shows a list of automations.
 */
public class AutomationListFragment extends ListFragment
{
    private AutomationListAdapter mAdapter;

    public AutomationListFragment()
    {
        // Obligatory empty constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mAdapter = new AutomationListAdapter(getActivity());
        setListAdapter(mAdapter);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.automation_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.action_add_automation:
            final View inputView =
                    getActivity().getLayoutInflater().inflate(R.layout.text_entry_view, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getActivity().getString(R.string.title_new_automation));
            builder.setMessage(getActivity().getString(R.string.message_new_automation));
            builder.setView(inputView);
            builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    dialogInterface.dismiss();
                    EditText input = (EditText) inputView.findViewById(R.id.text_input);
                    final String name = input.getText().toString();

                    // Bind the automation service and create the new automation.
                    Intent intent = new Intent(getActivity(), AutomationService.class);
                    getActivity().bindService(intent, new ServiceConnection()
                    {
                        @Override
                        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
                        {
                            AutomationService.LocalBinder binder =
                                    (AutomationService.LocalBinder) iBinder;
                            binder.addAutomation(name);
                            getActivity().unbindService(this);
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName componentName)
                        {

                        }
                    }, Context.BIND_AUTO_CREATE);
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
            return true;
        }
        return false;
    }

    public class AutomationListAdapter extends BaseAdapter implements ServiceConnection
    {
        // TODO: Implement this stuff.

        private LayoutInflater mInflater;

        private AutomationService.LocalBinder mBinder;

        public AutomationListAdapter(Context parent)
        {
            assert parent != null;
            assert parent.getApplicationContext() != null;

            mInflater = LayoutInflater.from(parent);

            Intent intent = new Intent(parent, AutomationService.class);
            parent.bindService(intent, this, Context.BIND_AUTO_CREATE);
        }

        @Override
        public int getCount()
        {
            if (mBinder == null) return 0;
            else return mBinder.getAutomationList().length;
        }

        @Override
        public Object getItem(int i)
        {
            if (mBinder == null) return null;
            else return mBinder.getAutomationList()[i];
        }

        @Override
        public long getItemId(int i)
        {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            if (mBinder == null) return null;
            else
            {
                Automation automation = mBinder.getAutomationList()[i];

                view = mInflater.inflate(R.layout.automation_list_item, null);

                TextView itemNameView = (TextView) view.findViewById(R.id.automation_name_view);
                TextView itemDescView = (TextView) view.findViewById(R.id.automation_desc_view);

                itemNameView.setText(automation.getName());

                String description = automation.getDescription();
                if (description.isEmpty()) itemDescView.setText(getActivity()
                                                                        .getString(R.string.automation_no_description));
                else itemDescView.setText(description);

                return view;
            }
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            mBinder = (AutomationService.LocalBinder) iBinder;
            notifyDataSetChanged();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mBinder = null;
            notifyDataSetChanged();
        }
    }
}
