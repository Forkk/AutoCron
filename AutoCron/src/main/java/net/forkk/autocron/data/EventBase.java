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


/**
 * Base class for events.
 */
public class EventBase extends AutomationBase implements Event
{
    public static final String LOGGER_TAG = AutomationService.LOGGER_TAG;

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
}
