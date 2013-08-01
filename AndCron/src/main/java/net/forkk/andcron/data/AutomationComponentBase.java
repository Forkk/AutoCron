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

package net.forkk.andcron.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;


/**
 * Abstract base class for automation components that implements all of their core functionality.
 */
public abstract class AutomationComponentBase extends ConfigComponentBase
        implements AutomationComponent
{
    protected Automation mAutomation;

    protected ArrayList<ComponentChangeListener> mListeners;

    public AutomationComponentBase(Automation parent, Context context, int id)
    {
        super(context, id);
        mAutomation = parent;

        mListeners = new ArrayList<ComponentChangeListener>();
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
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
        for (ComponentChangeListener listener : mListeners)
            listener.onComponentChange();
    }

    @Override
    public void addChangeListener(ComponentChangeListener listener)
    {
        mListeners.add(listener);
    }

    @Override
    public void removeChangeListener(ComponentChangeListener listener)
    {
        mListeners.remove(listener);
    }

    public interface ComponentChangeListener
    {
        public abstract void onComponentChange();
    }
}
