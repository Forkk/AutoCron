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
 * An action that displays a notification in the UI.
 */
public class NotificationAction extends ActionBase
{
    private static ActionType sComponentType;

    private static final String NOTIFICATION_TAG = "net.forkk.autocron.notification_action";

    private Notification mNotification;

    public static ActionType initComponentType(Resources res)
    {
        return sComponentType = new ActionType(res.getString(R.string.notification_action_title),
                                               res.getString(R.string.notification_action_description),
                                               NotificationAction.class);
    }

    public static ActionType getComponentType()
    {
        return sComponentType;
    }

    public NotificationAction(Automation parent, AutomationService service, int id)
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
        final AutomationService service = getService();
        NotificationManager notificationManager =
                (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_TAG, getId());
    }

    /**
     * Called when the action's automation has been activated. This should perform whatever this
     * action is meant to do on activation.
     */
    @Override
    public void onActivate()
    {
        final AutomationService service = getService();

        SharedPreferences prefs = getSharedPreferences();

        String title = prefs.getString("notification_title", getResources()
                                                                     .getString(R.string.pref_notification_action_title_default));
        String text = prefs.getString("notification_text", getResources()
                                                                   .getString(R.string.pref_notification_action_text_default));
        boolean isOngoing = prefs.getBoolean("notification_ongoing", false);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(service);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setOngoing(isOngoing);
        builder.setSmallIcon(R.drawable.ic_notification_icon);

        mNotification = builder.build();

        NotificationManager notificationManager =
                (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_TAG, getId(), mNotification);
    }

    /**
     * Called when the action's automation deactivates.
     */
    @Override
    public void onDeactivate()
    {
        final AutomationService service = getService();

        NotificationManager notificationManager =
                (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_TAG, getId());
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
