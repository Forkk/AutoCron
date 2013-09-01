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

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;


/**
 * Service that listens for NFC tags and forwards them to their corresponding rules.
 */
public class NfcService extends Service
{
    public static final String LOGGER_TAG = "net.forkk.autocron.NfcService";

    public static final String EXTRA_NFC_LISTENER_ID = "net.forkk.autocron.nfc_listener_id";

    public static final String EXTRA_NFC_LISTENER_ACTION = "net.forkk.autocron.nfc_listener_action";

    private Map<String, NfcListener> mListeners;

    public NfcService()
    {
        mListeners = new HashMap<String, NfcListener>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // I don't know why, but sometimes intent is null, and this check fixes it.
        // It works, don't question it.
        if (intent != null)
        {
            String id = intent.getStringExtra(EXTRA_NFC_LISTENER_ID);

            if (mListeners.containsKey(id))
                mListeners.get(id).tagTriggered(intent.getStringExtra(EXTRA_NFC_LISTENER_ACTION));
            else Log.w(LOGGER_TAG, "Unknown NFC tag ID encountered.");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return new LocalBinder();
    }

    public class LocalBinder extends Binder
    {
        public void registerListener(String id, NfcListener listener)
        {
            mListeners.put(id, listener);
        }

        public void unregisterListener(String id)
        {
            mListeners.remove(id);
        }
    }

    public interface NfcListener
    {
        public abstract void tagTriggered(String action);
    }
}
