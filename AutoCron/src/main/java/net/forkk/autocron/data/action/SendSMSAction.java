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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.preference.PreferenceFragment;
import android.telephony.SmsManager;
import android.util.Log;

import net.forkk.autocron.R;
import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentType;


/**
 * An action that sends an SMS message.
 */
public class SendSMSAction extends TriggerAction
{
    private static final String LOGGER_TAG = AutomationService.LOGGER_TAG;

    private static ActionType sComponentType;

    public static final String ACTION_MESSAGE_SEND_FINISHED =
            "net.forkk.autocron.ACTION_MESSAGE_SEND_FINISHED";

    public static final String EXTRA_REQUEST_ID = "net.forkk.autocron.REQUEST_ID";

    private static int nextSentPendingId = 0;

    public static ActionType initComponentType(Resources res)
    {
        return sComponentType = new ActionType(res.getString(R.string.send_sms_action_title),
                                               res.getString(R.string.send_sms_action_description),
                                               SendSMSAction.class);
    }

    public static ActionType getComponentType()
    {
        return sComponentType;
    }

    public SendSMSAction(Automation parent, AutomationService service, int id)
    {
        super(parent, service, id);
    }

    private static int getNewRequestId()
    {
        return nextSentPendingId++;
    }

    @Override
    public void onTrigger()
    {
        // TODO: Handle errors with sending SMS.
        final AutomationService service = getService();

        SharedPreferences preferences = getSharedPreferences();

        final String smsDestAddress = preferences.getString("sms_destination", "");
        final String smsContent = preferences.getString("sms_content", "");

        if (!smsDestAddress.isEmpty())
        {
            final int requestId = getNewRequestId();

            Intent intent = new Intent(ACTION_MESSAGE_SEND_FINISHED);
            intent.putExtra(EXTRA_REQUEST_ID, requestId);
            PendingIntent sentIntent = PendingIntent.getBroadcast(service, requestId, intent,
                                                                  PendingIntent.FLAG_ONE_SHOT);

            service.registerReceiver(new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    service.unregisterReceiver(this);

                    int receivedId = intent.getIntExtra(EXTRA_REQUEST_ID, -1);
                    if (requestId == receivedId)
                    {
                        switch (getResultCode())
                        {
                        case Activity.RESULT_OK:
                            // Add the sent message to the messages app.
                            ContentValues msg = new ContentValues();
                            msg.put("address", smsDestAddress);
                            msg.put("body", smsContent);
                            service.getContentResolver()
                                   .insert(Uri.parse("content://sms/sent"), msg);
                            Log.d(LOGGER_TAG, "SMS sent.");
                            break;

                        // TODO: Alert user of error.
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Log.e(LOGGER_TAG, "Failed to send SMS. Radio is off.");
                            break;

                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Log.e(LOGGER_TAG, "Failed to send SMS. No service.");
                            break;

                        default:
                            Log.e(LOGGER_TAG, "Failed to send SMS. An unknown error occurred.");
                            break;
                        }
                    }
                }
            }, new IntentFilter(ACTION_MESSAGE_SEND_FINISHED));

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(smsDestAddress, null, smsContent, sentIntent, null);
        }
    }

    /**
     * Called after the automation service finishes loading components. This should perform all
     * necessary initialization for this component.
     */
    @Override
    public void onCreate()
    {

    }

    /**
     * Called when the automation service is destroyed. This should perform all necessary cleanup.
     */
    @Override
    public void onDestroy()
    {

    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_sms_action);
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
}
