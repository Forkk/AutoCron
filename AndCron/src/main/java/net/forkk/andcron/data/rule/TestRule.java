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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import net.forkk.andcron.R;
import net.forkk.andcron.data.AutomationService;


/**
 * A simple test rule that activates when
 */
public class TestRule extends RuleBase implements AutomationService.IntentListener
{
    private Notification mNotification;

    private int mIntentListenerId;

    /**
     * Checks if this rule is active.
     *
     * @return If the rule is active, true, else false.
     */
    @Override
    public boolean isActive()
    {
        return false;
    }

    /**
     * Called after the automation service finishes loading components. This should perform all
     * necessary initialization for this component.
     *
     * @param context
     *         Context to initialize with.
     */
    @Override
    public void onCreate(Context context)
    {
        mIntentListenerId = getParent().getService().registerIntentListener(this);

        NotificationCompat.Builder mNotificationBuilder =
                new NotificationCompat.Builder(context).setContentTitle("Click to test")
                                                       .setContentText("Click to test the test rule.")
                                                       .setSmallIcon(R.drawable.ic_launcher);

        Intent notificationIntent = new Intent(context, AutomationService.class);
        notificationIntent.putExtra(AutomationService.LISTENER_ID_EXTRA, mIntentListenerId);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, notificationIntent,
                                                               PendingIntent.FLAG_CANCEL_CURRENT);
        mNotificationBuilder.setContentIntent(pendingIntent);

        mNotification = mNotificationBuilder.build();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(-1, mNotification);
    }

    /**
     * Called when the automation service is destroyed. This should perform all necessary cleanup.
     */
    @Override
    public void onDestroy()
    {

    }

    @Override
    public void onCommandReceived(Intent intent)
    {
        // TODO: Implement a better system for rules that fire once, rather than just activate and deactivate.
        setActive(true);
        setActive(false);
    }
}
