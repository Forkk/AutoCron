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

package net.forkk.autocron.data;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;


/**
 * Activity with no UI that handles NFC tags.
 */
public class NfcHandler extends Activity
{
    public static final String LOGGER_TAG = NfcService.LOGGER_TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        assert intent != null;
        String type = intent.getType();
        if (type != null && type.equals("application/net.forkk.autocron"))
        {
            // Read the first record which contains the NFC data
            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            assert messages != null;

            NdefMessage message = ((NdefMessage) messages[0]);
            NdefRecord[] records = message.getRecords();

            if (records.length < 1)
            {
                Log.e(LOGGER_TAG, "Error processing NFC tag. Data is invalid. Missing records.");
                finish();
                return;
            }

            NdefRecord idRecord = records[0];

            try
            {
                String id = new String(idRecord.getPayload());

                Intent serviceIntent = new Intent(this, NfcService.class);
                serviceIntent.putExtra(NfcService.EXTRA_NFC_LISTENER_ID, id);
                startService(serviceIntent);
            }
            catch (NumberFormatException e)
            {
                Log.e(LOGGER_TAG, "Error processing NFC tag. Data is invalid.", e);
                finish();
                return;
            }
        }

        finish();
    }
}
