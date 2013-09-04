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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import net.forkk.autocron.data.trigger.Trigger;
import net.forkk.autocron.data.trigger.TriggerType;

import java.util.ArrayList;
import java.util.List;


/**
 * Base class for events.
 */
public class EventBase extends AutomationBase implements Event
{
    public static final String LOGGER_TAG = AutomationService.LOGGER_TAG;

    private static final String VALUE_TRIGGER_IDS = "trigger_ids";

    protected final TriggerTypeInterface mTriggerTypeInterface = new TriggerTypeInterface();

    protected List<Trigger> mTriggers;

    /**
     * Loads a new event from the given SharedPreferences.
     *
     * @param service
     *         The automation service.
     * @param preferencesId
     *         The ID of the shared preferences to load from.
     *
     * @return The new event.
     */
    public static Event fromSharedPreferences(AutomationService service, int preferencesId)
    {
        return new EventBase(service, preferencesId);
    }

    public static Event createNewEvent(String name, AutomationService service, int preferencesId)
    {
        EventBase event = new EventBase(service, preferencesId);
        SharedPreferences.Editor edit = event.getSharedPreferences().edit();
        edit.clear().commit();
        edit.putString(VALUE_NAME, name);
        edit.commit();
        return event;
    }

    public EventBase(AutomationService service, int sharedPreferencesId)
    {
        super(service, sharedPreferencesId);
        mTriggers = new ArrayList<Trigger>();
        loadConfig(service);
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
        return "event_" + id;
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

    /**
     * @return An array of this automation's triggers.
     */
    @Override
    public List<Trigger> getTriggers()
    {
        return mTriggerTypeInterface.getList();
    }

    /**
     * Adds a new trigger of the given type with the given name.
     *
     * @param type
     *         The type of trigger to add.
     */
    @Override
    public Trigger addTrigger(TriggerType type)
    {
        return addComponent(type, mTriggerTypeInterface);
    }

    /**
     * Removes the trigger with the given ID.
     *
     * @param id
     *         The ID of the trigger to remove.
     */
    @Override
    public void deleteTrigger(int id)
    {
        deleteComponent(id, mTriggerTypeInterface);
    }

    /**
     * Tries to find a trigger with the given ID.
     *
     * @param id
     *         The trigger ID to search for.
     *
     * @return The trigger with the given ID if one exists, otherwise null.
     */
    @Override
    public Trigger findTriggerById(int id)
    {
        for (Trigger trigger : mTriggers)
            if (trigger.getId() == id) return trigger;
        return null;
    }


    @Override
    protected void loadConfig(Context context)
    {
        Log.i(LOGGER_TAG, "Loading automation configuration for \"" + getName() + "\".");

        SharedPreferences prefs = getSharedPreferences();

        loadComponentList(context, prefs, mRuleTypeInterface);

        loadComponentList(context, prefs, mActionTypeInterface);

        loadComponentList(context, prefs, mTriggerTypeInterface);

        onComponentListChange();

        Log.i(LOGGER_TAG, "Done loading automation configuration for \"" + getName() + "\".");
    }

    protected class TriggerTypeInterface implements ComponentTypeInterface<Trigger>
    {
        @Override
        public String getTypeName(boolean upper)
        {
            return upper ? "Trigger" : "action";
        }

        @Override
        public String getIdListKey()
        {
            return VALUE_TRIGGER_IDS;
        }

        @Override
        public List<Trigger> getList()
        {
            return mTriggers;
        }

        @Override
        public Trigger loadFromPrefs(Automation automation, Context context, int id)
        {
            return TriggerType.fromSharedPreferences(automation, context, id);
        }

        @Override
        public Trigger findById(int id)
        {
            return findTriggerById(id);
        }
    }
}
