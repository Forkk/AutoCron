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
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.EditText;

import net.forkk.autocron.R;
import net.forkk.autocron.util.InputFilterMinMax;

import java.util.ArrayList;
import java.util.Collections;


/**
 * An EditTextPreference for editing floats.
 */
public class EditFloatPreference extends EditTextPreference
{
    protected Float mMin;

    protected Float mMax;

    public EditFloatPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        try
        {
            TypedArray values =
                    context.obtainStyledAttributes(attrs, R.styleable.EditFloatPreference);
            assert values != null;

            mMin = null;
            mMax = null;

            if (values.hasValue(R.styleable.EditFloatPreference_minValue))
                mMin = values.getFloat(R.styleable.EditFloatPreference_minValue, 0);

            if (values.hasValue(R.styleable.EditFloatPreference_maxValue))
                mMax = values.getFloat(R.styleable.EditFloatPreference_maxValue, 0);

            if (mMin != null && mMax != null && mMin > mMax)
                throw new IllegalArgumentException("The minimum value should be less than the maximum value.");

            if (mMin != null || mMax != null)
            {
                EditText edit = getEditText();
                assert edit != null;

                ArrayList<InputFilter> filters = new ArrayList<InputFilter>();
                Collections.addAll(filters, edit.getFilters());
                filters.add(new InputFilterMinMax(mMin, mMax));
                edit.setFilters(filters.toArray(new InputFilter[filters.size()]));
            }
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Min and max values must be valid floats.", e);
        }
    }

    @Override
    protected String getPersistedString(String defaultReturnValue)
    {
        float defaultValue = 0;
        if (defaultReturnValue != null) defaultValue = Float.valueOf(defaultReturnValue);

        return String.valueOf(getPersistedFloat(defaultValue));
    }

    @Override
    protected boolean persistString(String strValue)
    {
        float value;

        try
        {
            value = Float.parseFloat(strValue);
        }
        catch (NumberFormatException e)
        {
            value = 0;
            if (mMax != null && value > mMax) value = mMax;
            if (mMin != null && value < mMin) value = mMin;
        }

        return persistFloat(value);
    }
}
