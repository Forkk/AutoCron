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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceFragment;

import net.forkk.autocron.R;
import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentType;


/**
 * A rule that activates or deactivates based on the current WiFi network.
 */
public class WiFiRule extends RuleBase
{
    private static RuleType sComponentType;

    private WiFiRuleBroadcastReceiver mReceiver;

    public static RuleType initComponentType(Resources res)
    {
        return sComponentType = new RuleType(res.getString(R.string.wifi_rule_title),
                                             res.getString(R.string.wifi_rule_description),
                                             WiFiRule.class);
    }

    public static RuleType getComponentType()
    {
        return sComponentType;
    }

    public WiFiRule(Automation parent, AutomationService service, int id)
    {
        super(parent, service, id);
        mReceiver = new WiFiRuleBroadcastReceiver();
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
     * Gets the SSID that the user has specified this rule should match.
     */
    protected String getMatchSSID()
    {
        return getSharedPreferences().getString("ssid", "");
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
        service.registerReceiver(mReceiver,
                                 new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
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
        service.unregisterReceiver(mReceiver);
    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_wifi_rule);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String s)
    {
        super.onSharedPreferenceChanged(preferences, s);
        updateState();
    }

    protected void updateState()
    {
        ConnectivityManager connManager =
                (ConnectivityManager) getService().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        assert netInfo != null;

        if (!netInfo.isConnected())
        {
            setActive(false);
            return;
        }

        WifiManager wifiManager = (WifiManager) getService().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        String ssid = wifiInfo.getSSID();

        if (ssid.startsWith("\"") && ssid.endsWith("\""))
            ssid = ssid.substring(1, ssid.length() - 1);

        setActive(getMatchSSID().isEmpty() || ssid.equals(getMatchSSID()));
    }

    class WiFiRuleBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            updateState();
        }
    }
}
