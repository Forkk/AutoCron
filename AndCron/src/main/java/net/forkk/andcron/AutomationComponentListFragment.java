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

import net.forkk.andcron.data.Automation;
import net.forkk.andcron.data.ConfigComponent;

import java.util.ArrayList;
import java.util.List;


/**
 * List fragment for listing components of an automation.
 */
public class AutomationComponentListFragment extends ComponentListFragment
{
    private Automation mAutomation;

    private ComponentListType mType;

    public AutomationComponentListFragment(Automation automation, ComponentListType type)
    {
        mType = type;
        mAutomation = automation;
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
        // TODO: Implement this.
        return null;
    }

    @Override
    protected void onAddComponent(String name)
    {
        // TODO: Implement adding components.
    }

    @Override
    protected void onEditComponent(int position, long id)
    {
        // TODO: Implement editing these components.
    }

    @Override
    protected void onDeleteComponent(int id)
    {
        // TODO: Implement deleting these components.
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

    public enum ComponentListType
    {
        Rule,
        Action
    }
}
