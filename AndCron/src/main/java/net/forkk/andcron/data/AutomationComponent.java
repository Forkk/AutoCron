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

/**
 * An interface for components of an automation. Components are things such as rules and actions.
 */
public interface AutomationComponent extends ConfigComponent
{
    public abstract void addChangeListener(ComponentChangeListener listener);

    public abstract void removeChangeListener(ComponentChangeListener listener);

    /**
     * Called when this component's parent automation is disabled. This should call destroy on the
     * component, causing the component's onDestroy function to be called if the component is
     * enabled.
     */
    public abstract void onParentDisabled();

    /**
     * Gets this component's type name. That is, the name of this component type as it should be
     * displayed in the user interface.
     *
     * @return The component's type name.
     */
    public abstract String getTypeName();

    /**
     * Class for listening to changes to the component's configuration.
     */
    public interface ComponentChangeListener
    {
        public abstract void onComponentChange();
    }
}
