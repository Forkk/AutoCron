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

package net.forkk.autocron.data.trigger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import net.forkk.autocron.NfcRuleWriteActivity;
import net.forkk.autocron.R;
import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentType;
import net.forkk.autocron.data.NfcService;


/**
 * Trigger that is set off by a certain NFC tag.
 */
public class NfcTrigger extends TriggerBase implements NfcService.NfcListener
{
    private static final String VALUE_NFC_TAG_ID = "nfc_tag_id";

    private static TriggerType sComponentType;

    private String mRegisteredId;

    public static TriggerType initComponentType(Resources res)
    {
        return sComponentType = new TriggerType(res.getString(R.string.nfc_trigger_title),
                                                res.getString(R.string.nfc_trigger_description),
                                                NfcTrigger.class);
    }

    public static TriggerType getComponentType()
    {
        return sComponentType;
    }

    @Override
    public ComponentType getType()
    {
        return getComponentType();
    }


    public NfcTrigger(Automation parent, AutomationService service, int id)
    {
        super(parent, service, id);
    }


    @Override
    protected void onCreate()
    {
        registerListener();
    }

    @Override
    protected void onDestroy()
    {
        unregisterListener();
    }


    protected void registerListener()
    {
        final AutomationService service = getService();
        assert service != null;

        final String tagId = getTagId();

        if (mRegisteredId != null && mRegisteredId.equals(tagId)) return;

        Intent serviceIntent = new Intent(service, NfcService.class);
        service.bindService(serviceIntent, new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder)
            {
                NfcService.LocalBinder binder = (NfcService.LocalBinder) iBinder;
                if (mRegisteredId != null)
                    binder.unregisterListener(mRegisteredId, NfcTrigger.this);
                binder.registerListener(tagId, NfcTrigger.this);
                mRegisteredId = tagId;

                service.unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName)
            {

            }
        }, Context.BIND_AUTO_CREATE);
    }

    protected void unregisterListener()
    {
        final AutomationService service = getService();
        assert service != null;

        if (mRegisteredId == null) return;

        Intent serviceIntent = new Intent(service, NfcService.class);
        service.bindService(serviceIntent, new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder)
            {
                NfcService.LocalBinder binder = (NfcService.LocalBinder) iBinder;
                binder.unregisterListener(mRegisteredId, NfcTrigger.this);
                mRegisteredId = null;

                service.unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName)
            {

            }
        }, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void tagTriggered()
    {
        trigger();
    }


    public String getTagId()
    {
        String id = getSharedPreferences().getString(VALUE_NFC_TAG_ID, "");
        if (id.isEmpty()) return Integer.toString(getId());
        else return id;
    }


    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_nfc_trigger);

        String idString = getTagId();

        Preference writeTag = fragment.findPreference("write");

        assert writeTag != null;

        Intent writeTagIntent = new Intent(getService(), NfcRuleWriteActivity.class);

        writeTagIntent.putExtra(NfcRuleWriteActivity.EXTRA_NFC_TAG_ID, idString);

        writeTag.setIntent(writeTagIntent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
    {
        super.onSharedPreferenceChanged(preferences, key);
        if (key.equals(VALUE_NFC_TAG_ID)) registerListener();
    }
}
