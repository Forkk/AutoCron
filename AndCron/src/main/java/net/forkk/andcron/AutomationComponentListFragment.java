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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.forkk.andcron.data.Automation;
import net.forkk.andcron.data.AutomationImpl;
import net.forkk.andcron.data.ComponentType;
import net.forkk.andcron.data.ConfigComponent;
import net.forkk.andcron.data.action.Action;
import net.forkk.andcron.data.action.ActionType;
import net.forkk.andcron.data.rule.Rule;
import net.forkk.andcron.data.rule.RuleType;

import java.util.ArrayList;
import java.util.List;


/**
 * List fragment for listing components of an automation.
 */
public class AutomationComponentListFragment extends ComponentListFragment
        implements AutomationImpl.ComponentListChangeListener
{
    private Automation mAutomation;

    private ComponentListType mType;

    public AutomationComponentListFragment(Automation automation, ComponentListType type)
    {
        mType = type;
        mAutomation = automation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mAutomation.registerComponentListObserver(this);
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
    protected List<ConfigComponent> getComponentList()
    {
        switch (mType)
        {
        case Rule:
            return new ArrayList<ConfigComponent>(mAutomation.getRules());

        case Action:
            return new ArrayList<ConfigComponent>(mAutomation.getActions());
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
        assert adapter != null;

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
        intent.putExtra(EditAutomationActivity.EXTRA_AUTOMATION_ID, mAutomation.getId());
        intent.putExtra(EditComponentActivity.EXTRA_COMPONENT_ID, (int) id);

        switch (mType)
        {
        case Rule:
            intent.putExtra(EditComponentActivity.EXTRA_COMPONENT_TYPE, 0);
            break;
        case Action:
            intent.putExtra(EditComponentActivity.EXTRA_COMPONENT_TYPE, 1);
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
