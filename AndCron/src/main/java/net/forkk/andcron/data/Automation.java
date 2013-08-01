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

import net.forkk.andcron.data.action.Action;
import net.forkk.andcron.data.action.ActionType;
import net.forkk.andcron.data.rule.Rule;
import net.forkk.andcron.data.rule.RuleType;

import java.util.List;


/**
 * Interface for automations. Provides access to all of the necessary stuff.
 */
public interface Automation extends ConfigComponent
{
    /**
     * @return An array of this automation's rules.
     */
    public List<Rule> getRules();

    /**
     * Adds a new rule of the given type with the given name.
     *
     * @param name
     *         Name of the rule to add.
     * @param type
     *         The type of rule to add.
     */
    public void addRule(String name, RuleType type);

    /**
     * Removes the rule with the given ID.
     *
     * @param id
     *         The ID of the rule to remove.
     */
    public void deleteRule(int id);

    /**
     * @return An array of this automation's actions.
     */
    public List<Action> getActions();

    /**
     * Adds a new action with the given name.
     *
     * @param name
     *         Name of the action to add.
     * @param type
     *         The type of action to add.
     */
    public void addAction(String name, ActionType type);

    /**
     * Removes the action with the given ID.
     *
     * @param id
     *         The ID of the action to remove.
     */
    public void deleteAction(int id);

    /**
     * @return True or false depending on whether or not the automation is active.
     */
    public boolean isActive();

    /**
     * Called when a rule is activated or deactivated. This should recheck all of the rules and see
     * if the automation should be activated.
     */
    public void updateActivationState();

    /**
     * @return The automation service that this automation is attached to.
     */
    public AutomationService getService();

    void registerComponentListObserver(AutomationImpl.ComponentListChangeListener listener);

    void unregisterComponentListObserver(AutomationImpl.ComponentListChangeListener listener);
}
