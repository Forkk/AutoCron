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

import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationComponent;
import net.forkk.autocron.data.AutomationComponentPointer;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ConfigComponent;
import net.forkk.autocron.data.Event;

import java.io.Serializable;


/**
 * Interface for a trigger component. A trigger is a component that can trigger an event
 * automation.
 */
public interface Trigger extends AutomationComponent
{
    /**
     * Registers the given trigger listener to this trigger.
     *
     * @param listener
     *         The listener to register.
     */
    public abstract void registerTriggerListener(TriggerListener listener);

    /**
     * Un-registers the given trigger listener from this trigger.
     *
     * @param listener
     *         The listener to register.
     */
    public abstract void unregisterTriggerListener(TriggerListener listener);

    /**
     * An interface that allows an object to listen for when this trigger is triggered.
     */
    public interface TriggerListener
    {
        /**
         * Called when a trigger this listener is registered to becomes triggered.
         *
         * @param trigger
         *         The trigger that was triggered.
         */
        public abstract void onTriggered(Trigger trigger);
    }

    public static class Pointer extends AutomationComponentPointer implements Serializable
    {
        protected int mTriggerId;

        public Pointer(Trigger trigger)
        {
            super(trigger.getParent().getId(), AutomationType.Event);
            mTriggerId = trigger.getId();
        }

        public Pointer(Automation automation, int triggerId)
        {
            super(automation);
            mTriggerId = triggerId;
        }

        public Pointer(int automationId, int triggerId)
        {
            super(automationId, AutomationType.Event);
            mTriggerId = triggerId;
        }

        @Override
        public ConfigComponent getComponent(AutomationService.LocalBinder binder)
        {
            Event parent = (Event) getAutomation(binder);
            if (parent == null) return null;
            return parent.findTriggerById(mTriggerId);
        }
    }
}
