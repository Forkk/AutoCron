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

package net.forkk.autocron.prefs;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;


/**
 * An EditTextPreference for editing floats.
 */
public class EditFloatPreference extends EditTextPreference
{
    public EditFloatPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue)
    {
        float defaultValue = 0F;
        if (defaultReturnValue != null) defaultValue = Float.valueOf(defaultReturnValue);

        return String.valueOf(getPersistedFloat(defaultValue));
    }

    @Override
    protected boolean persistString(String value)
    {
        return persistFloat(Float.valueOf(value));
    }
}