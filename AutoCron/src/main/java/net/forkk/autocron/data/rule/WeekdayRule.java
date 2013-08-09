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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceFragment;
import android.util.Log;

import net.forkk.autocron.R;
import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentType;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;


/**
 * A rule that activates on certain days of the week.
 */
public class WeekdayRule extends RuleBase implements AutomationService.IntentListener
{
    private static RuleType sComponentType;

    private int mIntentListenerId = -1;

    private PendingIntent mUpdateIntent;

    public static RuleType initComponentType(Resources res)
    {
        return sComponentType = new RuleType(res.getString(R.string.weekday_rule_title),
                                             res.getString(R.string.weekday_rule_description),
                                             WeekdayRule.class);
    }

    public static RuleType getComponentType()
    {
        return sComponentType;
    }

    public WeekdayRule(Automation parent, AutomationService service, int id)
    {
        super(parent, service, id);
    }

    /**
     * Gets this automation's component type. This should return the same object for all components
     * of this type.
     *
     * @return The component type object for this component.
     */
    @Override
    public ComponentType getType()
    {
        return getComponentType();
    }

    /**
     * Gets a set of integers representing which weekdays this rule should be active on. Each
     * integer corresponds to a weekday constant from the Calendar API.
     */
    public Set<Integer> getActiveWeekdays()
    {
        SharedPreferences prefs = getSharedPreferences();
        Set<String> weekdays = prefs.getStringSet("weekdays", new HashSet<String>());
        Set<Integer> weekdayIntegers = new HashSet<Integer>();

        for (String weekday : weekdays)
        {
            // I wish this were Java 7...
            if (weekday.equals("monday")) weekdayIntegers.add(Calendar.MONDAY);
            else if (weekday.equals("tuesday")) weekdayIntegers.add(Calendar.TUESDAY);
            else if (weekday.equals("wednesday")) weekdayIntegers.add(Calendar.WEDNESDAY);
            else if (weekday.equals("thursday")) weekdayIntegers.add(Calendar.THURSDAY);
            else if (weekday.equals("friday")) weekdayIntegers.add(Calendar.FRIDAY);
            else if (weekday.equals("saturday")) weekdayIntegers.add(Calendar.SATURDAY);
            else if (weekday.equals("sunday")) weekdayIntegers.add(Calendar.SUNDAY);
            else Log.w(AutomationService.LOGGER_TAG, "Unknown weekday encountered: " + weekday);
        }

        return weekdayIntegers;
    }

    /**
     * Gets the next time that this rule should check to see if it is active.
     */
    public long getUpdateTime()
    {
        // This is simple. Just update at the beginning of the next day.
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, +1);
        return calendar.getTimeInMillis();
    }

    /**
     * Called after the automation service finishes loading components. This should perform all
     * necessary initialization for this component.
     *
     * @param service
     *         The automation service that this component belongs to.
     */
    @Override
    protected void onCreate(AutomationService service)
    {
        mIntentListenerId = service.registerIntentListener(this);
        setAlarm();
        updateState();
    }

    /**
     * Called when the automation service is destroyed. This should perform all necessary cleanup.
     *
     * @param service
     *         The automation service that this component belongs to.
     */
    @Override
    protected void onDestroy(AutomationService service)
    {
        AlarmManager alarmManager = (AlarmManager) service.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.cancel(mUpdateIntent);

        service.unregisterIntentListener(mIntentListenerId);
        mIntentListenerId = -1;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
    {
        super.onSharedPreferenceChanged(preferences, key);
        if (key.equals("weekdays")) updateState();
    }

    /**
     * Sets the rule's alarm to update it.
     */
    protected void setAlarm()
    {
        AlarmManager alarmManager =
                (AlarmManager) getService().getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;

        Intent intent = new Intent(getService(), AutomationService.class);
        intent.putExtra(AutomationService.LISTENER_ID_EXTRA, mIntentListenerId);
        mUpdateIntent = PendingIntent.getService(getService(), mIntentListenerId, intent,
                                                 PendingIntent.FLAG_ONE_SHOT);

        alarmManager
                .setRepeating(AlarmManager.RTC_WAKEUP, getUpdateTime(), AlarmManager.INTERVAL_DAY,
                              mUpdateIntent);
    }

    /**
     * Updates this rule's state.
     */
    public void updateState()
    {
        setActive(getActiveWeekdays().contains(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)));
    }

    @Override
    public void onCommandReceived(Intent intent)
    {
        updateState();
    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_weekday_rule);
    }
}
