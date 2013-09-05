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

import net.forkk.autocron.data.action.Action;
import net.forkk.autocron.data.action.ActionType;
import net.forkk.autocron.data.rule.Rule;
import net.forkk.autocron.data.rule.RuleType;

import java.util.List;


/**
 * Interface for automations. Includes things that states and triggers share in common.
 */
public interface Automation extends ConfigComponent
{
    /**
     * @return The automation service that this automation is attached to.
     */
    public AutomationService getService();


    void registerComponentListObserver(StateBase.ComponentListChangeListener listener);

    void unregisterComponentListObserver(StateBase.ComponentListChangeListener listener);

    public static interface ComponentListChangeListener
    {
        public void onComponentListChange();
    }


    /**
     * @return An array of this automation's rules.
     */
    public List<Rule> getRules();

    /**
     * Adds a new rule of the given type with the given name.
     *
     * @param type
     *         The type of rule to add.
     */
    public Rule addRule(RuleType type);

    /**
     * Removes the rule with the given ID.
     *
     * @param id
     *         The ID of the rule to remove.
     */
    public void deleteRule(int id);

    /**
     * Tries to find a rule with the given ID.
     *
     * @param id
     *         The rule ID to search for.
     *
     * @return The rule with the given ID if one exists, otherwise null.
     */
    public Rule findRuleById(int id);

    /**
     * @return An array of this automation's actions.
     */
    public List<Action> getActions();

    /**
     * Adds a new action with the given name.
     *
     * @param type
     *         The type of action to add.
     */
    public Action addAction(ActionType type);

    /**
     * Removes the action with the given ID.
     *
     * @param id
     *         The ID of the action to remove.
     */
    public void deleteAction(int id);

    /**
     * Tries to find an action with the given ID.
     *
     * @param id
     *         The action ID to search for.
     *
     * @return The action with the given ID if one exists, otherwise null.
     */
    public Action findActionById(int id);

    /**
     * Checks if the given component type is compatible with this automation type.
     */
    public boolean isComponentTypeCompatible(ComponentType<? extends AutomationComponent> type);

    /**
     * Reloads all components from configuration.
     * <p/>
     * This is generally called when some configuration options change. It re-creates all of the
     * rules and actions.
     */
    public void reloadComponents(AutomationService service);
}
