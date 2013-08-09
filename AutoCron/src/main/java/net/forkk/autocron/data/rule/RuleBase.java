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

import android.preference.PreferenceFragment;

import net.forkk.autocron.R;
import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationComponentBase;
import net.forkk.autocron.data.AutomationService;


/**
 * Abstract base class for rules that implements common functionality.
 */
public abstract class RuleBase extends AutomationComponentBase implements Rule
{
    protected boolean mIsActive;

    public RuleBase(Automation parent, AutomationService service, int id)
    {
        super(parent, service, id);
    }

    public void setActive(boolean active)
    {
        mIsActive = active;
        getParent().updateActivationState();
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        getParent().updateActivationState();
    }

    @Override
    public boolean isActive()
    {
        return mIsActive;
    }

    @Override
    protected String getSharedPreferencesName(int id)
    {
        return "rule_" + id;
    }

    protected static String getSharedPreferencesNameForId(int id)
    {
        return "rule_" + id;
    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_rule);
    }
}
