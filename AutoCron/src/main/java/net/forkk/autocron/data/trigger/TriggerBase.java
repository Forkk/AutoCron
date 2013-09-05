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

package net.forkk.autocron.data.trigger;

import android.preference.PreferenceFragment;

import net.forkk.autocron.R;
import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationComponentBase;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentPointer;

import java.util.ArrayList;
import java.util.List;


/**
 * Base class for triggers.
 */
public abstract class TriggerBase extends AutomationComponentBase implements Trigger
{
    protected List<TriggerListener> mTriggerListeners;

    public TriggerBase(Automation parent, AutomationService service, int id)
    {
        super(parent, service, id);
        mTriggerListeners = new ArrayList<TriggerListener>();
    }

    /**
     * This should be called to trigger this trigger component. Calls onTriggered for all listeners. 
     */
    protected void trigger()
    {
        for (TriggerListener listener : mTriggerListeners)
            listener.onTriggered(this);
    }

    /**
     * Gets this component's shared preferences name for the given component ID.
     *
     * @param id
     *         The component's ID.
     *
     * @return The shared preferences ID for the corresponding ID.
     */
    @Override
    protected String getSharedPreferencesName(int id)
    {
        return "trigger_" + id;
    }

    public static String getSharedPreferencesNameForId(int id)
    {
        return "trigger_" + id;
    }


    @Override
    public void registerTriggerListener(TriggerListener listener)
    {
        mTriggerListeners.add(listener);
    }

    @Override
    public void unregisterTriggerListener(TriggerListener listener)
    {
        mTriggerListeners.remove(listener);
    }


    /**
     * Gets a component pointer that points to this component.
     *
     * @return A component pointer pointing to this component or null if this component type doesn't
     * support component pointers.
     */
    @Override
    public ComponentPointer getPointer()
    {
        return new Pointer(this);
    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_trigger);
    }
}
