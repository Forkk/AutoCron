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

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Abstract base for all config components.
 */
public abstract class ConfigComponentBase implements ConfigComponent
{
    public static final String VALUE_NAME = "name";

    public static final String VALUE_DESCRIPTION = "description";

    private SharedPreferences mPreferences;

    private int mId;

    public ConfigComponentBase(Context context, int id)
    {
        mId = id;
        mPreferences =
                context.getSharedPreferences(getSharedPreferencesId(id), Context.MODE_PRIVATE);
    }

    /**
     * Gets this component's shared preferences name for the given component ID.
     *
     * @param id
     *         The component's ID.
     *
     * @return The shared preferences ID for the corresponding ID.
     */
    protected abstract String getSharedPreferencesId(int id);

    protected SharedPreferences getSharedPreferences()
    {
        return mPreferences;
    }

    @Override
    public int getId()
    {
        return mId;
    }

    /**
     * @return The user-given name for this component.
     */
    @Override
    public String getName()
    {
        return getSharedPreferences().getString(VALUE_NAME, "Unnamed");
    }

    /**
     * @return The user-given description for this component.
     */
    @Override
    public String getDescription()
    {
        return getSharedPreferences().getString(VALUE_DESCRIPTION, "");
    }
}
