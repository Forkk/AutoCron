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

package net.forkk.autocron.data;

import android.content.SharedPreferences;

import java.util.ArrayList;


/**
 * Abstract base class for automation components that implements all of their core functionality.
 */
public abstract class AutomationComponentBase extends ConfigComponentBase
        implements AutomationComponent
{
    protected Automation mAutomation;

    protected ArrayList<AutomationComponent.ComponentChangeListener> mListeners;

    public AutomationComponentBase(Automation parent, AutomationService service, int id)
    {
        super(service, id);
        mAutomation = parent;

        mListeners = new ArrayList<AutomationComponent.ComponentChangeListener>();
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void create(AutomationService service)
    {
        if (getParent().isEnabled()) super.create(service);
    }

    @Override
    public void destroy(AutomationService service)
    {
        if (getParent().isEnabled()) super.destroy(service);
    }

    @Override
    public String getName()
    {
        return getType().getTypeName();
    }

    @Override
    public void onParentDisabled()
    {
        super.destroy(getService());
    }

    /**
     * Gets this component's parent automation.
     */
    protected Automation getParent()
    {
        return mAutomation;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String s)
    {
        for (AutomationComponent.ComponentChangeListener listener : mListeners)
            listener.onComponentChange();
    }

    @Override
    public void addChangeListener(AutomationComponent.ComponentChangeListener listener)
    {
        mListeners.add(listener);
    }

    @Override
    public void removeChangeListener(AutomationComponent.ComponentChangeListener listener)
    {
        mListeners.remove(listener);
    }
}
