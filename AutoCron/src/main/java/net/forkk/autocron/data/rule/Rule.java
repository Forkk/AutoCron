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

package net.forkk.autocron.data.rule;

import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationComponent;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentPointer;
import net.forkk.autocron.data.ConfigComponent;

import java.io.Serializable;


/**
 * An automation rule that specifies when an automation should activate.
 */
public interface Rule extends AutomationComponent
{
    /**
     * Checks if this rule is active.
     *
     * @return If the rule is active, true, else false.
     */
    public abstract boolean isActive();

    /**
     * Checks if this is an inverted rule. If a rule is inverted, its active state is inverted when
     * determining if the automation should be active.
     */
    public abstract boolean isInverted();

    public static class Pointer implements ComponentPointer, Serializable
    {
        protected int mAutomationId;

        protected int mRuleId;

        public Pointer(Rule rule)
        {
            mAutomationId = rule.getParent().getId();
            mRuleId = rule.getId();
        }

        public Pointer(int automationId, int ruleId)
        {
            mAutomationId = automationId;
            mRuleId = ruleId;
        }

        @Override
        public ConfigComponent getComponent(AutomationService.LocalBinder binder)
        {
            Automation parent = binder.findAutomationById(mAutomationId);
            if (parent == null) return null;
            return parent.findRuleById(mRuleId);
        }
    }
}
