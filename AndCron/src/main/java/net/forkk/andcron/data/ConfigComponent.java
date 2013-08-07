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
     * Initializes the component, calling onCreate if it is enabled
     *
     * @param service
     *         The automation service that this component belongs to.
     */
    public abstract void create(AutomationService service);

    /**
     * Destroy the component, calling onDestroy if it is enabled
     *
     * @param service
     *         The automation service that this component belongs to.
     */
    public abstract void destroy(AutomationService service);

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
     * Enables or disables the component.
     * <p/>
     * onCreate is called when the component is enabled and onDestroy is called when it is
     * disabled.
     */
    public abstract void setEnabled(boolean enabled);

    /**
     * @return Whether this component is enabled or not.
     */
    public abstract boolean isEnabled();

    /**
     * Gets this component's shared preferences name.
     *
     * @return The shared preferences name for this component.
     */
    public abstract String getSharedPreferencesName();

    public abstract void addPreferencesToFragment(PreferenceFragment fragment);
}
