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
    public static class Pointer extends AutomationComponentPointer implements Serializable
    {
        protected int mTriggerId;

        public Pointer(Trigger trigger)
        {
            super(trigger.getParent().getId(), AutomationType.Event);
            mTriggerId = trigger.getId();
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
