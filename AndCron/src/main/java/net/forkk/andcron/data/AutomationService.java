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

package net.forkk.andcron.data;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Service class that handles all of the automation stuff.
 * <p/>
 * Responsible for getting the automation list as well.
 */
public class AutomationService extends Service
{
    public static final String LOGGER_TAG = "net.forkk.andcron.AutomationService";

    private static final String JSON_CONFIG_FILE = "automations.json";

    private static final int CONFIG_FILE_VERSION = 0;

    public static final String LISTENER_ID_EXTRA = "net.forkk.andcron.listener_id";

    private ArrayList<Automation> mAutomations;

    private Map<Integer, IntentListener> mIntentListenerMap;

    private int mCurrentListenerId;

    public AutomationService()
    {
        mIntentListenerMap = new HashMap<Integer, IntentListener>();
        mCurrentListenerId = 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId)
    {
        handleCommand(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        handleCommand(intent);
        return START_STICKY;
    }

    private void handleCommand(Intent intent)
    {
        int listenerId = intent.getIntExtra(LISTENER_ID_EXTRA, -1);
        if (mIntentListenerMap.containsKey(listenerId))
        {
            IntentListener listener = mIntentListenerMap.get(listenerId);
            listener.onCommandReceived(intent);
        }
        else Log.w(LOGGER_TAG, "Command received for unknown listener ID.");
    }

    /**
     * Registers the given intent listener with the automation service and returns its ID.
     *
     * @param listener
     *         The intent listener to add.
     *
     * @return The intent listener's ID. This should be passed as an extra to the intent, under the
     * key LISTENER_ID_EXTRA.
     */
    public int registerIntentListener(IntentListener listener)
    {
        mIntentListenerMap.put(mCurrentListenerId, listener);
        return mCurrentListenerId++;
    }

    /**
     * Un-registers the intent listener with the given ID.
     *
     * @param id
     *         The ID of the intent listener to remove.
     */
    public void unregisterIntentListener(int id)
    {
        mIntentListenerMap.remove(id);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        mAutomations = new ArrayList<Automation>();

        // Load automations from the config file.
        loadConfig();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // Save automations to config, just to be sure.
        saveConfig();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return (IBinder) new LocalBinder();
    }

    public void loadConfig()
    {
        Log.i(LOGGER_TAG, "Loading configuration.");

        String json = "";
        try
        {
            Log.i(LOGGER_TAG, "Loading JSON configuration file.");
            FileInputStream inputStream = openFileInput(JSON_CONFIG_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line + "\n");
            inputStream.close();
            json = sb.toString();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            Log.w(LOGGER_TAG, "JSON configuration file does not exist. It will be created when " +
                              "configuration is saved. Aborted loading.", e);
            return;
        }
        catch (IOException e)
        {
            // TODO: Alert user that their configuration file failed to load.
            Log.e(LOGGER_TAG, "Failed to read JSON configuration file. Aborted loading.", e);
            e.printStackTrace();
            return;
        }

        try
        {
            Log.i(LOGGER_TAG, "Parsing JSON configuration file.");
            JSONObject root = new JSONObject(json);

            int configVersion = root.getInt("version");
            if (configVersion != CONFIG_FILE_VERSION)
            {
                // TODO: Handle version mismatch properly by running upgrades or something.
                Log.e(LOGGER_TAG, "Configuration file version mismatch. Aborted loading.");
                return;
            }

            Log.i(LOGGER_TAG, "Getting automation list.");
            ArrayList<Automation> tempAutomationList = new ArrayList<Automation>();
            JSONArray automations = root.getJSONArray("automations");
            for (int i = 0; i < automations.length(); i++)
            {
                try
                {
                    Automation automation =
                            AutomationImpl.fromJSONObject(this, automations.getJSONObject(i));
                    Log.i(LOGGER_TAG, "Loaded automation " + automation.getName() + ".");
                    tempAutomationList.add(automation);
                }
                catch (JSONException e)
                {
                    // TODO: Handle warning the user that one of their automations' configs was corrupt.
                    // For now, just ignore things that failed to load.
                    Log.w(LOGGER_TAG, "Failed to load an automation from the config.", e);
                }
            }
            mAutomations.clear();
            mAutomations.addAll(tempAutomationList);
        }
        catch (JSONException e)
        {
            // TODO: Handle failed parsing of config file.
            e.printStackTrace();
            Log.e(LOGGER_TAG, "Failed to parse JSON configuration file. Aborted loading.", e);
            return;
        }

        Log.i(LOGGER_TAG, "Done loading configuration.");
    }

    public void saveConfig()
    {
        Log.i(LOGGER_TAG, "Saving configuration.");

        JSONObject root = null;
        try
        {
            root = new JSONObject();
            root.put("version", CONFIG_FILE_VERSION);

            JSONArray automations = new JSONArray();
            for (Automation automation : mAutomations)
            {
                try
                {
                    JSONObject object = new JSONObject();
                    automation.writeToJSONObject(object);
                    automations.put(object);
                }
                catch (JSONException e)
                {
                    // TODO: Alert user that the automation failed to save to JSON.
                    Log.w(LOGGER_TAG, "Failed to save automation " + automation.getName() + " to " +
                                      "JSON configuration file. Omitting.", e);
                }
            }

            root.put("automations", automations);
        }
        catch (JSONException e)
        {
            // TODO: Alert user that their configuration failed to save.
            Log.w(LOGGER_TAG, "Failed to save automations to JSON configuration file. Aborting", e);
            return;
        }

        try
        {
            FileOutputStream outputStream = openFileOutput(JSON_CONFIG_FILE, Context.MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(outputStream);
            writer.write(root.toString());
        }
        catch (FileNotFoundException e)
        {
            Log.wtf(LOGGER_TAG, "File that was just created doesn't exist.", e);
        }

        Log.i(LOGGER_TAG, "Done saving configuration.");
    }

    public class LocalBinder
    {
        public AutomationService getService()
        {
            return AutomationService.this;
        }
    }

    /**
     * An interface for classes that want to listen for intents to be passed as the service's start
     * command.
     */
    public static interface IntentListener
    {
        public void onCommandReceived(Intent intent);
    }
}
