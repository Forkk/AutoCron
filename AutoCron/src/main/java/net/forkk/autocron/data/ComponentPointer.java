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
 * A serializable class that "points" to a component, allowing it to be easily retrieved from the
 * automation service.
 */
public interface ComponentPointer extends Serializable
{
    /**
     * Gets the component this pointer points to from the automation service.
     *
     * @param binder
     *         The binder to use to retrieve the component from the service.
     *
     * @return The component this pointer points to, or null if it can't be found.
     */
    public abstract ConfigComponent getComponent(AutomationService.LocalBinder binder);
}
