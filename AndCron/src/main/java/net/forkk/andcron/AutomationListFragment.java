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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
                itemDescView.setText(automation.getDescription());

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
