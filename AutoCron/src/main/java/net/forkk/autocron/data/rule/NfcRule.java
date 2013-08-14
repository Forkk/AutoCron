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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import net.forkk.autocron.NfcRuleWriteActivity;
import net.forkk.autocron.R;
import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentType;
import net.forkk.autocron.data.NfcService;


/**
 * Rule that is activated by an NFC tag that contains the rule's ID.
 */
public class NfcRule extends RuleBase implements NfcService.NfcListener, ServiceConnection
{
    private static final String LOGGER_TAG = NfcService.LOGGER_TAG;

    private static RuleType sComponentType;

    private NfcService.LocalBinder mBinder;

    public static RuleType initComponentType(Resources res)
    {
        return sComponentType = new RuleType(res.getString(R.string.nfc_rule_title),
                                             res.getString(R.string.nfc_rule_description),
                                             NfcRule.class)
        {
            @Override
            public boolean checkIfSupported(Context context)
            {
                if (NfcAdapter.getDefaultAdapter(context) == null)
                {
                    setSupportError(context.getString(R.string.error_rule_requires_nfc));
                    return false;
                }
                return super.checkIfSupported(context);
            }
        };
    }

    public static RuleType getComponentType()
    {
        return sComponentType;
    }

    public NfcRule(Automation parent, AutomationService service, int id)
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
     * Called after the automation service finishes loading components. This should perform all
     * necessary initialization for this component.
     */
    @Override
    protected void onCreate()
    {
        final AutomationService service = getService();
        service.bindService(new Intent(service, NfcService.class), this, Context.BIND_AUTO_CREATE);
    }

    /**
     * Called when the automation service is destroyed. This should perform all necessary cleanup.
     */
    @Override
    protected void onDestroy()
    {
        final AutomationService service = getService();
        mBinder.unregisterListener(((Integer) getId()).toString());
        getService().unbindService(this);
    }

    @Override
    public void tagTriggered(String action)
    {
        if (action.equals("activate")) setActive(true);
        else if (action.equals("deactivate")) setActive(false);
        else if (action.equals("toggle")) setActive(!isActive());
        else Log.w(LOGGER_TAG, "Unknown NFC tag action: " + action);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        mBinder = (NfcService.LocalBinder) iBinder;
        mBinder.registerListener(((Integer) getId()).toString(), this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {

    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_nfc_rule);

        String idString = String.valueOf(getId());

        Preference writeActivate = fragment.findPreference("write_activate");
        Preference writeDeactivate = fragment.findPreference("write_deactivate");
        Preference writeToggle = fragment.findPreference("write_toggle");

        assert writeActivate != null;
        assert writeDeactivate != null;
        assert writeToggle != null;

        Intent writeActivateIntent = new Intent(getService(), NfcRuleWriteActivity.class);
        Intent writeDeactivateIntent = new Intent(getService(), NfcRuleWriteActivity.class);
        Intent writeToggleIntent = new Intent(getService(), NfcRuleWriteActivity.class);

        writeActivateIntent.putExtra(NfcRuleWriteActivity.EXTRA_NFC_RULE_ID, idString);
        writeActivateIntent.putExtra(NfcRuleWriteActivity.EXTRA_NFC_ACTION, "activate");

        writeDeactivateIntent.putExtra(NfcRuleWriteActivity.EXTRA_NFC_RULE_ID, idString);
        writeDeactivateIntent.putExtra(NfcRuleWriteActivity.EXTRA_NFC_ACTION, "deactivate");

        writeToggleIntent.putExtra(NfcRuleWriteActivity.EXTRA_NFC_RULE_ID, idString);
        writeToggleIntent.putExtra(NfcRuleWriteActivity.EXTRA_NFC_ACTION, "toggle");

        writeActivate.setIntent(writeActivateIntent);
        writeDeactivate.setIntent(writeDeactivateIntent);
        writeToggle.setIntent(writeToggleIntent);
    }
}
