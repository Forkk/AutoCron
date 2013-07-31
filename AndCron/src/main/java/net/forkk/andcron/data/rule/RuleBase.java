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

package net.forkk.andcron.data.rule;

import net.forkk.andcron.data.AutomationComponentBase;


/**
 * Abstract base class for rules that implements common functionality.
 */
public abstract class RuleBase extends AutomationComponentBase implements Rule
{
    protected boolean mIsActive;

    public RuleBase()
    {
        mIsActive = false;
    }

    public void setActive(boolean active)
    {
        mIsActive = active;
        if (active) getParent().updateActivationState();
    }

    @Override
    public boolean isActive()
    {
        return mIsActive;
    }
}