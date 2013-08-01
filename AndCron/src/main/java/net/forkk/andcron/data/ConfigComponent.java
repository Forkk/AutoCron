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

import android.preference.PreferenceFragment;


/**
 * Interface that provides access to the underlying data structure of components such as
 * automations, rules, actions, and exceptions.
 */
public interface ConfigComponent
{
    /**
     * Called after the automation service finishes loading components. This should perform all
     * necessary initialization for this component.
     *
     * @param service
     *         The automation service that this component belongs to.
     */
    public abstract void onCreate(AutomationService service);

    /**
     * Called when the automation service is destroyed. This should perform all necessary cleanup.
     *
     * @param service
     *         The automation service that this component belongs to.
     */
    public abstract void onDestroy(AutomationService service);

    /**
     * @return This component's ID.
     */
    public abstract int getId();

    /**
     * @return The user-given name for this component.
     */
    public abstract String getName();

    /**
     * @return The user-given description for this component.
     */
    public abstract String getDescription();

    /**
     * Gets this component's shared preferences name.
     *
     * @return The shared preferences name for this component.
     */
    public abstract String getSharedPreferencesName();

    public abstract void addPreferencesToFragment(PreferenceFragment fragment);
}
