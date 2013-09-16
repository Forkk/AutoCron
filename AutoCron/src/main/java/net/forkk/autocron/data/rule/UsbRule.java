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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.BatteryManager;

import net.forkk.autocron.R;
import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentType;


/**
 * Rule that activates while a USB cable is connected.
 */
public class UsbRule extends RuleBase
{
    private static RuleType sComponentType;

    public static RuleType initComponentType(Resources res)
    {
        return sComponentType = new RuleType(res.getString(R.string.usb_rule_title),
                                             res.getString(R.string.usb_rule_description),
                                             UsbRule.class);
    }

    public static RuleType getComponentType()
    {
        return sComponentType;
    }

    @Override
    public ComponentType getType()
    {
        return getComponentType();
    }


    public UsbRule(Automation parent, AutomationService service, int id)
    {
        super(parent, service, id);
    }


    protected String getConnectedAction()
    {
        return Intent.ACTION_POWER_CONNECTED;
    }

    protected String getDisconnectedAction()
    {
        return Intent.ACTION_POWER_DISCONNECTED;
    }


    @Override
    protected void onCreate()
    {
        final AutomationService service = getService();
        assert service != null;

        IntentFilter filter = new IntentFilter();
        filter.addAction(getConnectedAction());
        filter.addAction(getDisconnectedAction());

        // Register the USB receiver.
        service.registerReceiver(mUsbReceiver, filter);

        // Now we check if the device is already plugged in and, if so, activate the rule.
        Intent batteryIntent =
                service.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        assert batteryIntent != null;
        int plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        setActive(plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                  plugged == BatteryManager.BATTERY_PLUGGED_USB);
    }

    @Override
    protected void onDestroy()
    {
        final AutomationService service = getService();
        assert service != null;

        // Unregister the USB receiver.
        service.unregisterReceiver(mUsbReceiver);
    }

    protected BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action == null) return;

            if (action.equals(getConnectedAction()))
            {
                setActive(true);
            }
            else if (action.equals(getDisconnectedAction()))
            {
                setActive(false);
            }
        }
    };
}
