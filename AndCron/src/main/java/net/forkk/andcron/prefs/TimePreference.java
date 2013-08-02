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

package net.forkk.andcron.prefs;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import net.forkk.andcron.R;


/**
 * Preference that stores a time.
 */
public class TimePreference extends DialogPreference
{
    private int lastHour = 0;

    private int lastMinute = 0;

    private TimePicker picker = null;

    public static int getHour(String time)
    {
        String[] pieces = time.split(":");

        return Integer.parseInt(pieces[0]);
    }

    public static int getMinute(String time)
    {
        String[] pieces = time.split(":");

        return Integer.parseInt(pieces[1]);
    }

    public TimePreference(Context context, AttributeSet attributes)
    {
        super(context, attributes);

        setPositiveButtonText(R.string.okay);
        setNegativeButtonText(R.string.cancel);
    }

    @Override
    protected View onCreateDialogView()
    {
        picker = new TimePicker(getContext());

        return (picker);
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        picker.setCurrentHour(lastHour);
        picker.setCurrentMinute(lastMinute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        super.onDialogClosed(positiveResult);

        if (positiveResult)
        {
            lastHour = picker.getCurrentHour();
            lastMinute = picker.getCurrentMinute();

            String time = String.valueOf(lastHour) + ":" + String.valueOf(lastMinute);

            if (callChangeListener(time))
            {
                persistString(time);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray values, int index)
    {
        return (values.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        String time;

        if (restoreValue)
        {
            if (defaultValue == null)
            {
                time = getPersistedString("00:00");
            }
            else
            {
                time = getPersistedString(defaultValue.toString());
            }
        }
        else
        {
            time = defaultValue.toString();
        }

        lastHour = getHour(time);
        lastMinute = getMinute(time);
    }
}
