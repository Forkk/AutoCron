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
    protected void onAddComponent(final String name)
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
                switch (mType)
                {
                case Rule:
                    mAutomation.addRule(name, RuleType.getRuleTypes()[i]);
                    break;

                case Action:
                    mAutomation.addAction(name, ActionType.getActionTypes()[i]);
                    break;
                }
            }
        });

        builder.show();
    }

    @Override
    protected void onEditComponent(int position, long id)
    {
        // TODO: Implement editing these components.
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

            view = mInflater.inflate(android.R.layout.simple_list_item_activated_2, null);

            assert view != null;
            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            TextView text2 = (TextView) view.findViewById(android.R.id.text2);

            text1.setText(type.getTypeName());
            text2.setText(type.getTypeDesc());

            return view;
        }
    }
}
