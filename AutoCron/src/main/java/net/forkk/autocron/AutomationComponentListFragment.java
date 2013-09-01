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
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentType;
import net.forkk.autocron.data.ConfigComponent;
import net.forkk.autocron.data.State;
import net.forkk.autocron.data.StateBase;
import net.forkk.autocron.data.action.Action;
import net.forkk.autocron.data.action.ActionType;
import net.forkk.autocron.data.rule.Rule;
import net.forkk.autocron.data.rule.RuleType;

import java.util.ArrayList;
import java.util.List;


/**
 * List fragment for listing components of an automation.
 */
public class AutomationComponentListFragment extends ComponentListFragment
        implements StateBase.ComponentListChangeListener, ServiceConnection
{
    private static final String VALUE_AUTOMATION_POINTER = "net.forkk.autocron.automation_id";

    private static final String VALUE_COMPONENT_TYPE = "net.forkk.autocron.component_type";

    private State mAutomation;

    private State.Pointer mAutomationPointer;

    private ComponentListType mType;

    public AutomationComponentListFragment()
    {

    }

    public AutomationComponentListFragment(State.Pointer automationPointer, ComponentListType type)
    {
        Bundle arguments = new Bundle();
        arguments.putSerializable(VALUE_AUTOMATION_POINTER, automationPointer);
        arguments.putSerializable(VALUE_COMPONENT_TYPE, type);
        setArguments(arguments);
    }

    private void loadAutomation()
    {
        Activity activity = getActivity();
        assert activity != null;
        activity.bindService(new Intent(activity, AutomationService.class), this,
                             Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        assert arguments != null;

        mAutomationPointer = (State.Pointer) arguments.getSerializable(VALUE_AUTOMATION_POINTER);
        mType = (ComponentListType) arguments.getSerializable(VALUE_COMPONENT_TYPE);

        loadAutomation();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mAutomation.unregisterComponentListObserver(this);
    }

    /**
     * @return An array of the components that this list should contain.
     */
    @Override
    protected List<? extends ConfigComponent> getComponentList()
    {
        if (mAutomation == null) return new ArrayList<ConfigComponent>();

        switch (mType)
        {
        case Rule:
            // When mAutomation gets set, update the list.
            return mAutomation.getRules();

        case Action:
            return mAutomation.getActions();
        }
        return null;
    }

    /**
     * @return Whether or not there are items in the list.
     */
    @Override
    protected boolean hasItems()
    {
        return !getComponentList().isEmpty();
    }

    /**
     * Finds the component with the given ID.
     */
    @Override
    protected ConfigComponent findComponentById(int id)
    {
        switch (mType)
        {
        case Rule:
            for (Rule rule : mAutomation.getRules())
                if (rule.getId() == id) return rule;
            return null;

        case Action:
            for (Action action : mAutomation.getActions())
                if (action.getId() == id) return action;
            return null;
        }
        return null;
    }

    @Override
    protected void onActionAddComponent()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.title_choose_type,
                                                  getComponentTypeName(false)));

        ComponentTypeAdapter adapter = null;
        switch (mType)
        {
        case Rule:
            adapter = new ComponentTypeAdapter(getActivity(), RuleType.getRuleTypes());
            break;

        case Action:
            adapter = new ComponentTypeAdapter(getActivity(), ActionType.getActionTypes());
            break;
        }

        builder.setAdapter(adapter, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                ConfigComponent component = null;
                switch (mType)
                {
                case Rule:
                    component = mAutomation.addRule(RuleType.getRuleTypes()[i]);
                    break;

                case Action:
                    component = mAutomation.addAction(ActionType.getActionTypes()[i]);
                    break;
                }
                onEditComponent(component.getId());
            }
        });

        builder.show();
    }

    @Override
    protected void onEditComponent(long id)
    {
        Intent intent = new Intent(getActivity(), EditComponentActivity.class);

        switch (mType)
        {
        case Rule:
            intent.putExtra(EditComponentActivity.EXTRA_COMPONENT_POINTER,
                            new Rule.Pointer(mAutomation.getId(), (int) id));
            break;
        case Action:
            intent.putExtra(EditComponentActivity.EXTRA_COMPONENT_POINTER,
                            new Action.Pointer(mAutomation.getId(), (int) id));
            break;
        }

        startActivity(intent);
    }

    @Override
    protected void onDeleteComponent(int id)
    {
        switch (mType)
        {
        case Rule:
            mAutomation.deleteRule(id);
            break;
        case Action:
            mAutomation.deleteAction(id);
            break;
        }
    }

    @Override
    public String getComponentTypeName(boolean upper)
    {
        switch (mType)
        {
        case Rule:
            return upper ? getResources().getString(R.string.rule_upper)
                         : getResources().getString(R.string.rule_lower);

        case Action:
            return upper ? getResources().getString(R.string.action_upper)
                         : getResources().getString(R.string.action_lower);
        }
        return null;
    }

    @Override
    public void onComponentListChange()
    {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        AutomationService.LocalBinder binder = (AutomationService.LocalBinder) iBinder;

        mAutomation = (State) mAutomationPointer.getComponent(binder);
        assert mAutomation != null;
        mAutomation.registerComponentListObserver(this);

        Activity activity = getActivity();
        assert activity != null;
        activity.unbindService(this);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {

    }

    public enum ComponentListType
    {
        Rule,
        Action
    }

    private class ComponentTypeAdapter extends BaseAdapter
    {
        LayoutInflater mInflater;

        ComponentType[] mComponentTypes;

        public ComponentTypeAdapter(Context parent, ComponentType[] types)
        {
            mInflater = LayoutInflater.from(parent);
            mComponentTypes = types;
        }

        @Override
        public int getCount()
        {
            return mComponentTypes.length;
        }

        @Override
        public Object getItem(int i)
        {
            return mComponentTypes[i];
        }

        @Override
        public long getItemId(int i)
        {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            ComponentType type = mComponentTypes[i];

            view = mInflater.inflate(R.layout.component_type_list_item, null);

            assert view != null;
            TextView titleView = (TextView) view.findViewById(R.id.text_view_type_name);
            TextView descView = (TextView) view.findViewById(R.id.text_view_type_description);
            TextView errorView = (TextView) view.findViewById(R.id.text_view_type_error);

            titleView.setText(type.getTypeName());
            descView.setText(type.getTypeDesc());

            if (!type.isSupported())
            {
                errorView.setText(type.getSupportError());
                errorView.setVisibility(View.VISIBLE);
                view.setBackgroundColor(Color.LTGRAY);
            }

            return view;
        }

        @Override
        public int getItemViewType(int position)
        {
            ComponentType type = mComponentTypes[position];
            return type.isSupported() ? 0 : 1;
        }

        @Override
        public int getViewTypeCount()
        {
            return 2;
        }

        @Override
        public boolean isEnabled(int position)
        {
            ComponentType type = mComponentTypes[position];
            return type.isSupported();
        }
    }
}
