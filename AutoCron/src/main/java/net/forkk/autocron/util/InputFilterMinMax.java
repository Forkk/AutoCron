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

package net.forkk.autocron.util;

import android.text.InputFilter;
import android.text.Spanned;


/**
 * Input filter that limits the minimum and maximum values of a float input.
 */
public class InputFilterMinMax implements InputFilter
{
    protected Float mMin;

    protected Float mMax;

    /**
     * Creates a new min max input filter.
     *
     * @param min
     *         The minimum value or null for no minimum value.
     * @param max
     *         The maximum value or null for no maximum value.
     */
    public InputFilterMinMax(Float min, Float max)
    {
        if (min != null && max != null && min > max)
            throw new IllegalArgumentException("The minimum value should be less than the maximum value.");

        mMin = min;
        mMax = max;
    }

    @Override
    public CharSequence filter(CharSequence source, int sStart, int sEnd, Spanned dest, int dStart,
                               int dEnd)
    {
        String resultStr = buildResult(source, dest, sStart, sEnd, dStart, dEnd);

        // It is necessary to allow users to enter a - so that negative numbers can be entered.
        if (resultStr.equals("-")) return null;

        try
        {
            float value = Float.parseFloat(resultStr);
            if ((mMin != null && value < mMin) || (mMax != null && value > mMax)) return "";
            else return null;
        }
        catch (NumberFormatException e)
        {
            return "";
        }
    }

    protected String buildResult(CharSequence source, CharSequence dest, int sStart, int sEnd,
                                 int dStart, int dEnd)
    {
        CharSequence startSequence = dest.subSequence(0, dStart);
        CharSequence endSequence = dest.subSequence(dEnd, dest.length());
        CharSequence insertSequence = source.subSequence(sStart, sEnd);
        return startSequence.toString() + insertSequence.toString() + endSequence.toString();
    }
}
