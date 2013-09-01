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

package net.forkk.autocron.data.action;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceFragment;
import android.support.v4.app.NotificationCompat;

import net.forkk.autocron.R;
import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentType;


/**
 * A trigger action that displays a notification when it is triggered.
 */
public class TriggerNotificationAction extends TriggerAction
{
    private static final String NOTIFICATION_TAG = "net.forkk.autocron.TriggerNotificationAction";

    private static ActionType sComponentType;

    public static ActionType initComponentType(Resources res)
    {
        return sComponentType =
                       new ActionType(res.getString(R.string.trigger_notification_action_title),
                                      res.getString(R.string.trigger_notification_action_description),
                                      TriggerNotificationAction.class);
    }

    public static ActionType getComponentType()
    {
        return sComponentType;
    }

    public TriggerNotificationAction(Automation parent, AutomationService service, int id)
    {
        super(parent, service, id);
    }

    /**
     * Called after the automation service finishes loading components. This should perform all
     * necessary initialization for this component.
     */
    @Override
    protected void onCreate()
    {

    }

    /**
     * Called when the automation service is destroyed. This should perform all necessary cleanup.
     */
    @Override
    protected void onDestroy()
    {

    }

    @Override
    public void onTrigger()
    {
        final AutomationService service = getService();

        SharedPreferences prefs = getSharedPreferences();

        String defTitle = getResources().getString(R.string.pref_notification_action_title_default);
        String defText = getResources().getString(R.string.pref_notification_action_text_default);

        String title = prefs.getString("notification_title", defTitle);
        String text = prefs.getString("notification_text", defText);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(service);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setOngoing(false);
        builder.setSmallIcon(R.drawable.ic_notification_icon);

        Notification notification = builder.build();

        NotificationManager notificationManager =
                (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_TAG, getId(), notification);
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

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_notification_action);
    }
}
