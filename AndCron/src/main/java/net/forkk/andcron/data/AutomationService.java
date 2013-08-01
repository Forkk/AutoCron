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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Service class that handles all of the automation stuff.
 * <p/>
 * Responsible for getting the automation list as well.
 */
public class AutomationService extends Service
        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String LOGGER_TAG = "net.forkk.andcron.AutomationService";

    public static final String LISTENER_ID_EXTRA = "net.forkk.andcron.listener_id";

    /**
     * Name of the SharedPreferences that stores automations.
     */
    private static final String PREF_AUTOMATIONS = "automations";

    private static final String VALUE_AUTOMATION_IDS = "automation_ids";

    private ArrayList<Automation> mAutomations;

    private Map<Integer, IntentListener> mIntentListenerMap;

    private ArrayList<AutomationListChangeListener> mAutomationListChangeListeners;

    private int mCurrentListenerId;

    public AutomationService()
    {
        mIntentListenerMap = new HashMap<Integer, IntentListener>();
        mAutomationListChangeListeners = new ArrayList<AutomationListChangeListener>();
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
        if (intent == null) return;

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
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return new LocalBinder();
    }

    public void loadConfig()
    {
        Log.i(LOGGER_TAG, "Loading configuration.");

        SharedPreferences prefs = getSharedPreferences(PREF_AUTOMATIONS, MODE_PRIVATE);
        Set<String> automationIDs = prefs.getStringSet(VALUE_AUTOMATION_IDS, new HashSet<String>());

        ArrayList<Automation> tempAutomationList = new ArrayList<Automation>();
        for (String stringVal : automationIDs)
        {
            try
            {
                int id = Integer.parseInt(stringVal);
                Automation automation = AutomationImpl.fromSharedPreferences(this, id);
                tempAutomationList.add(automation);
                Log.i(LOGGER_TAG, "Loaded automation \"" + automation.getName() + "\".");
            }
            catch (NumberFormatException e)
            {
                Log.e(LOGGER_TAG, "Found non-integer in automation ID set.", e);
            }
        }

        for (Automation automation : mAutomations)
            getSharedPreferences(automation.getSharedPreferencesName(), MODE_PRIVATE)
                    .unregisterOnSharedPreferenceChangeListener(this);
        mAutomations.clear();
        mAutomations.addAll(tempAutomationList);
        for (Automation automation : mAutomations)
            getSharedPreferences(automation.getSharedPreferencesName(), MODE_PRIVATE)
                    .registerOnSharedPreferenceChangeListener(this);

        Log.i(LOGGER_TAG, "Done loading configuration.");

        onAutomationListChange();
    }

    /**
     * Adds the given automation's ID to the automation ID list.
     *
     * @param automation
     *         The automation to add.
     */
    public void addAutomation(Automation automation)
    {
        SharedPreferences prefs = getSharedPreferences(PREF_AUTOMATIONS, MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();

        Set<String> automationIDs = new HashSet<String>();
        automationIDs.add(((Integer) automation.getId()).toString());
        automationIDs.addAll(prefs.getStringSet(VALUE_AUTOMATION_IDS, new HashSet<String>()));

        edit.putStringSet(VALUE_AUTOMATION_IDS, automationIDs);
        mAutomations.add(automation);
        boolean success = edit.commit();
        if (!success) Log.w(LOGGER_TAG, "Failed to commit changes to preferences.");
        else onAutomationListChange();

        assert prefs.getStringSet(VALUE_AUTOMATION_IDS, new HashSet<String>())
                    .equals(automationIDs);
    }

    private void deleteAutomation(int id)
    {
        Automation automation = findAutomationById(id);
        if (automation == null)
        {
            Log.e(LOGGER_TAG, "Attempted to delete automation that didn't exist.");
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREF_AUTOMATIONS, MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();

        Set<String> automationIDs = new HashSet<String>();
        automationIDs.addAll(prefs.getStringSet(VALUE_AUTOMATION_IDS, new HashSet<String>()));
        automationIDs.remove(((Integer) id).toString());

        edit.putStringSet(VALUE_AUTOMATION_IDS, automationIDs);
        mAutomations.remove(automation);
        boolean success = edit.commit();
        if (!success) Log.w(LOGGER_TAG, "Failed to commit changes to preferences.");
        else onAutomationListChange();
    }

    private Automation findAutomationById(int id)
    {
        for (Automation automation : mAutomations)
            if (automation.getId() == id) return automation;
        return null;
    }

    public int getUnusedAutomationId()
    {
        SharedPreferences prefs = getSharedPreferences(PREF_AUTOMATIONS, MODE_PRIVATE);
        Set<String> usedAutomationIds =
                prefs.getStringSet(VALUE_AUTOMATION_IDS, new HashSet<String>());

        int greatestValue = 0;
        for (String stringVal : usedAutomationIds)
        {
            try
            {
                int value = Integer.parseInt(stringVal);
                if (value > greatestValue) greatestValue = value;
            }
            catch (NumberFormatException e)
            {
                Log.w(LOGGER_TAG, "Found non-integer in automation ID set.", e);
            }
        }

        // To get an unused ID, we just find an ID value greater than the greatest one.
        return greatestValue + 1;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
    {
        if (key.equals("name") || key.equals("description")) onAutomationListChange();
    }

    public class LocalBinder extends Binder
    {
        public List<Automation> getAutomationList()
        {
            return mAutomations;
        }

        /**
         * Attempts to get the automation with the given ID.
         *
         * @param id
         *         The ID of the automation to find.
         *
         * @return The automation with the given ID or null if no automation was found.
         */
        public Automation findAutomationById(int id)
        {
            return AutomationService.this.findAutomationById(id);
        }

        /**
         * Creates a new automation and adds it to the automation list.
         *
         * @param name
         *         The name of the new automation.
         */
        public void createNewAutomation(String name)
        {
            Automation automation = AutomationImpl.createNewAutomation(name, AutomationService.this,
                                                                       getUnusedAutomationId());
            addAutomation(automation);
        }

        /**
         * Tries to delete the automation with the given ID.
         *
         * @param id
         *         The ID of the automation to delete.
         */
        public void deleteAutomation(int id)
        {
            AutomationService.this.deleteAutomation(id);
        }

        public void registerAutomationListChangeListener(AutomationListChangeListener listener)
        {
            mAutomationListChangeListeners.add(listener);
        }

        public void unregisterAutomationListChangeListener(AutomationListChangeListener listener)
        {
            mAutomationListChangeListeners.remove(listener);
        }
    }

    public void onAutomationListChange()
    {
        for (AutomationListChangeListener listener : mAutomationListChangeListeners)
        {
            if (listener != null) listener.onAutomationListChange();
        }
    }

    /**
     * An interface for classes that want to listen for changes to the automation list.
     */
    public static interface AutomationListChangeListener
    {
        public void onAutomationListChange();
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
