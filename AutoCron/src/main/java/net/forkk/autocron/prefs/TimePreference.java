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
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import net.forkk.autocron.R;


/**
 * Preference that stores a time.
 */
public class TimePreference extends DialogPreference
{
    private int mHour;

    private int mMinute;

    private TimePicker mTimePicker;

    public TimePreference(Context context, AttributeSet attributes)
    {
        super(context, attributes);

        setPositiveButtonText(R.string.okay);
        setNegativeButtonText(R.string.cancel);
    }

    public void setValues(int hour, int minute)
    {
        mHour = hour;
        mMinute = minute;
    }

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

    @Override
    protected View onCreateDialogView()
    {
        mTimePicker = new TimePicker(getContext());

        return mTimePicker;
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        mTimePicker.setCurrentHour(mHour);
        mTimePicker.setCurrentMinute(mMinute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        super.onDialogClosed(positiveResult);

        if (positiveResult)
        {
            mHour = mTimePicker.getCurrentHour();
            mMinute = mTimePicker.getCurrentMinute();

            String time = String.valueOf(mHour) + ":" + String.valueOf(mMinute);

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

        if (restoreValue) time = getPersistedString("00:00");
        else time = defaultValue.toString();

        mHour = getHour(time);
        mMinute = getMinute(time);
    }
}
