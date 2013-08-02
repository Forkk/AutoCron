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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceFragment;

import net.forkk.andcron.R;
import net.forkk.andcron.data.Automation;
import net.forkk.andcron.data.AutomationService;

import java.util.Calendar;


/**
 * A rule that activates during a certain time range.
 */
public class TimeRangeRule extends RuleBase implements AutomationService.IntentListener
{
    private int mStartListenerId;

    private int mEndListenerId;

    private PendingIntent mPendingStartIntent;

    private PendingIntent mPendingEndIntent;

    public TimeRangeRule(Automation parent, Context context, int id)
    {
        super(parent, context, id);
    }

    protected long startTriggerTime()
    {
        return triggerTime("range_start");
    }

    protected long endTriggerTime()
    {
        return triggerTime("range_end");
    }

    protected long triggerTime(String key)
    {
        String[] pieces = getSharedPreferences().getString(key, "00:00").split(":");

        int hours = Integer.parseInt(pieces[0]);
        int minutes = Integer.parseInt(pieces[1]);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        return cal.getTimeInMillis();
    }

    /**
     * Called after the automation service finishes loading components. This should perform all
     * necessary initialization for this component.
     *
     * @param service
     *         The automation service that this component belongs to.
     */
    @Override
    public void onCreate(AutomationService service)
    {
        mStartListenerId = service.registerIntentListener(this);
        mEndListenerId = service.registerIntentListener(this);

        setAlarms(service);
    }

    /**
     * Called when the automation service is destroyed. This should perform all necessary cleanup.
     *
     * @param service
     *         The automation service that this component belongs to.
     */
    @Override
    public void onDestroy(AutomationService service)
    {
        AlarmManager alarmManager = (AlarmManager) service.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;

        alarmManager.cancel(mPendingStartIntent);
        alarmManager.cancel(mPendingEndIntent);
    }

    public void setAlarms(AutomationService service)
    {
        AlarmManager alarmManager = (AlarmManager) service.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;

        Intent startIntent = new Intent(service, AutomationService.class);
        startIntent.putExtra(AutomationService.LISTENER_ID_EXTRA, mStartListenerId);
        mPendingStartIntent = PendingIntent.getService(service, mStartListenerId, startIntent,
                                                       PendingIntent.FLAG_ONE_SHOT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTriggerTime(),
                                  AlarmManager.INTERVAL_DAY, mPendingStartIntent);

        Intent endIntent = new Intent(service, AutomationService.class);
        endIntent.putExtra(AutomationService.LISTENER_ID_EXTRA, mEndListenerId);
        mPendingEndIntent = PendingIntent.getService(service, mEndListenerId, endIntent,
                                                     PendingIntent.FLAG_ONE_SHOT);
        alarmManager
                .setRepeating(AlarmManager.RTC_WAKEUP, endTriggerTime(), AlarmManager.INTERVAL_DAY,
                              mPendingEndIntent);
    }

    @Override
    public void onCommandReceived(Intent intent)
    {
        int id = intent.getIntExtra(AutomationService.LISTENER_ID_EXTRA, -1);
        if (id == mStartListenerId)
        {
            // Just make sure we aren't past the end time.
            // This prevents quick activation and then deactivation when the service starts up.
            if (endTriggerTime() > System.currentTimeMillis()) setActive(true);
        }
        else if (id == mEndListenerId) setActive(false);
    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_time_range_rule);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String s)
    {
        super.onSharedPreferenceChanged(preferences, s);

        if (startTriggerTime() >= endTriggerTime())
        {
            SharedPreferences.Editor edit = preferences.edit();

            // If the end time is less than or equal to the start time, set the end time to the
            // start time plus one minute.
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(startTriggerTime());
            cal.add(Calendar.MINUTE, 1);

            Integer hour = cal.get(Calendar.HOUR_OF_DAY);
            Integer minute = cal.get(Calendar.MINUTE);

            // FIXME: This doesn't update the value in the preferences fragment until it's reopened.
            edit.putString("range_end", hour.toString() + ":" + minute.toString());
            edit.commit();
        }
        else setAlarms(getParent().getService());
    }
}
