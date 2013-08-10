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

package net.forkk.autocron.data.action;

import net.forkk.autocron.data.AutomationComponent;


/**
 * Interface for actions performed by an automation.
 */
public interface Action extends AutomationComponent
{
    /**
     * Called when the action's automation has been activated. This should perform whatever this
     * action is meant to do on activation.
     */
    public void onActivate();

    /**
     * Called when the action's automation deactivates.
     */
    public void onDeactivate();
}
