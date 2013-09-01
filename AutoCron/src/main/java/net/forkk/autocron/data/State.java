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

import java.io.Serializable;


/**
 * Interface for states. Provides access to all of the necessary stuff.
 */
public interface State extends Automation
{
    /**
     * @return True or false depending on whether or not the state is active.
     */
    public boolean isActive();

    /**
     * Called when a rule is activated or deactivated. This should recheck all of the rules and see
     * if the automation should be activated.
     */
    public void updateActivationState();

    public static class Pointer implements ComponentPointer, Serializable
    {
        protected int mStateId;

        public Pointer(State state)
        {
            mStateId = state.getId();
        }

        public Pointer(int id)
        {
            mStateId = id;
        }

        @Override
        public ConfigComponent getComponent(AutomationService.LocalBinder binder)
        {
            return binder.findStateById(mStateId);
        }
    }
}
