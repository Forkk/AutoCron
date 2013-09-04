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

import net.forkk.autocron.data.trigger.Trigger;
import net.forkk.autocron.data.trigger.TriggerType;

import java.io.Serializable;
import java.util.List;


/**
 * An event is an automation that has a set of "triggers" that can trigger it. When the event is
 * triggered, it can run trigger actions.
 * <p/>
 * Events can also have rules that prevent the event from being triggered when they are inactive.
 */
public interface Event extends Automation
{
    /**
     * @return An array of this automation's triggers.
     */
    public List<Trigger> getTriggers();

    /**
     * Adds a new trigger of the given type with the given name.
     *
     * @param type
     *         The type of trigger to add.
     */
    public Trigger addTrigger(TriggerType type);

    /**
     * Removes the trigger with the given ID.
     *
     * @param id
     *         The ID of the trigger to remove.
     */
    public void deleteTrigger(int id);

    /**
     * Tries to find a trigger with the given ID.
     *
     * @param id
     *         The trigger ID to search for.
     *
     * @return The trigger with the given ID if one exists, otherwise null.
     */
    public Trigger findTriggerById(int id);

    public static class Pointer implements ComponentPointer, Serializable
    {
        protected int mEventId;

        public Pointer(Event event)
        {
            mEventId = event.getId();
        }

        public Pointer(int id)
        {
            mEventId = id;
        }

        @Override
        public ConfigComponent getComponent(AutomationService.LocalBinder binder)
        {
            return binder.findEventById(mEventId);
        }
    }
}
