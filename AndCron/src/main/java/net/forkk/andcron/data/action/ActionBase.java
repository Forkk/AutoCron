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

package net.forkk.andcron.data.action;

import android.content.Context;

import net.forkk.andcron.data.Automation;
import net.forkk.andcron.data.AutomationComponentBase;


/**
 * Abstract base class for actions that implements common functionality.
 */
public abstract class ActionBase extends AutomationComponentBase implements Action
{
    public ActionBase(Automation parent, Context context, int id)
    {
        super(parent, context, id);
    }

    @Override
    protected String getSharedPreferencesName(int id)
    {
        return "action_" + id;
    }

    protected static String getSharedPreferencesNameForId(int id)
    {
        return "action_" + id;
    }
}
