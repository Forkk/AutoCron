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
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import net.forkk.autocron.R;

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
    public static final String LOGGER_TAG = "net.forkk.autocron.AutomationService";

    public static final String LISTENER_ID_EXTRA = "net.forkk.autocron.listener_id";

    public static final int CONFIG_FORMAT_VERSION = 1;

    /**
     * Name of the SharedPreferences that stores automations.
     */
    public static final String PREF_AUTOMATIONS = "automations";

    private static final String VALUE_STATE_IDS = "state_ids";

    private static final String VALUE_EVENT_IDS = "event_ids";

    private static final String VALUE_CONFIG_VERSION = "config_version";

    private ArrayList<State> mStates;

    private ArrayList<Event> mEvents;

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
        Log.d(LOGGER_TAG, "Initializing automation service.");

        super.onCreate();

        mStates = new ArrayList<State>();

        mEvents = new ArrayList<Event>();

        // Load automations from the config file.
        loadConfig();

        for (State state : mStates)
        {
            if (state.isEnabled()) state.create();
        }

        for (Event event : mEvents)
        {
            if (event.isEnabled()) event.create();
        }
    }

    @Override
    public void onDestroy()
    {
        Log.d(LOGGER_TAG, "Stopping automation service.");

        super.onDestroy();

        for (State state : mStates)
        {
            state.destroy();
        }

        for (Event event : mEvents)
        {
            event.destroy();
        }
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

        Log.d(LOGGER_TAG, "Checking configuration version.");
        int currentVersion = prefs.getInt(VALUE_CONFIG_VERSION, 0);

        // Version 0 didn't store its version in the configs, so we need to do some extra stuff
        // to verify that it actually is version 0 and not just a blank config.
        if (currentVersion == 0 && !prefs.contains("automation_ids"))
        {
            // If it isn't actually version 0, then set currentVersion to -1.
            currentVersion = -1;
            Log.i(LOGGER_TAG, "Configuration is blank.");
        }
        else if (currentVersion < CONFIG_FORMAT_VERSION)
        {
            Log.i(LOGGER_TAG, "Detected old configuration format. Upgrading.");
            upgradeConfig(prefs, currentVersion);
        }
        else Log.i(LOGGER_TAG, "Configuration format is up to date (" + CONFIG_FORMAT_VERSION +
                               ").");

        Log.i(LOGGER_TAG, "Loading states.");
        loadAutomationList(prefs, mStateTypeInterface);

        Log.i(LOGGER_TAG, "Loading events.");
        loadAutomationList(prefs, mEventTypeInterface);

        Log.i(LOGGER_TAG, "Done loading configuration.");

        onAutomationListChange();
    }

    public <T extends Automation> void loadAutomationList(SharedPreferences prefs,
                                                          AutomationTypeInterface<T> type)
    {
        Set<String> ids = prefs.getStringSet(type.getIdListKey(), new HashSet<String>());

        ArrayList<T> tempList = new ArrayList<T>();
        for (String stringVal : ids)
        {
            try
            {
                int id = Integer.parseInt(stringVal);
                T automation = type.loadFromPrefs(id);
                tempList.add(automation);
                Log.d(LOGGER_TAG,
                      "Loaded " + type.getTypeName(false) + " \"" + automation.getName() + "\".");
            }
            catch (NumberFormatException e)
            {
                Log.e(LOGGER_TAG,
                      "Found non-integer value in " + type.getTypeName(false) + " ID set.", e);
            }
        }

        List<T> list = type.getList();
        for (T automation : list)
            getSharedPreferences(automation.getSharedPreferencesName(), MODE_PRIVATE)
                    .unregisterOnSharedPreferenceChangeListener(this);
        list.clear();
        list.addAll(tempList);
        for (T automation : list)
            getSharedPreferences(automation.getSharedPreferencesName(), MODE_PRIVATE)
                    .registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Upgrades the config format from the given old version to the current version.
     *
     * @param oldVersion
     *         The version to upgrade from.
     */
    protected void upgradeConfig(SharedPreferences prefs, int oldVersion)
    {
        int nextVersion = oldVersion;
        while (nextVersion < CONFIG_FORMAT_VERSION)
        {
            Log.d(LOGGER_TAG, "Configuration upgrade: Upgrading to version " + ++nextVersion + ".");
            doConfigMigration(prefs, nextVersion);
        }
    }

    /**
     * Upgrades the configuration from the version prior to the specified version.
     * <p/>
     * For example, calling this with a new version value of 1 will upgrade the config format from
     * version 0 to version 1.
     * <p/>
     * When upgradeConfig() is called, it calls this function for each version until the config
     * format is up to date.
     *
     * @param targetVersion
     *         The version to upgrade to.
     */
    protected void doConfigMigration(SharedPreferences prefs, int targetVersion)
    {
        assert targetVersion <= CONFIG_FORMAT_VERSION;

        switch (targetVersion)
        {
        case 1:
            SharedPreferences.Editor editor = prefs.edit();

            // Using hardcoded key names here because if the key name for state_ids changes in the
            // future, this code will still need to use the old name.

            // Rename the automation_ids value to state_ids
            Log.d(LOGGER_TAG, "Renaming automation_ids set to state_ids.");
            Set<String> stateIDs = prefs.getStringSet("automation_ids", new HashSet<String>());
            editor.putStringSet("state_ids", stateIDs);
            editor.remove("automation_ids");

            // Now, rename all of the automation_<id> shared preferences objects to state_<id>.
            Log.d(LOGGER_TAG, "Renaming shared preference files for automations.");
            for (String id : stateIDs)
            {
                Log.d(LOGGER_TAG, "Renaming shared preferences file for state " + id + ".");
                SharedPreferences oldPrefs = getSharedPreferences("automation_" + id, MODE_PRIVATE);
                SharedPreferences newPrefs = getSharedPreferences("state_" + id, MODE_PRIVATE);

                SharedPreferences.Editor stateEdit = newPrefs.edit();

                // Copy everything from the old preferences file to the new preferences file.

                // First, get all of the values.
                Map<String, ?> values = oldPrefs.getAll();
                assert values != null;

                // Then copy over each of them to the new preferences file. 
                for (Map.Entry<String, ?> entry : values.entrySet())
                {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof String) stateEdit.putString(key, (String) value);
                    else if (value instanceof Boolean) stateEdit.putBoolean(key, (Boolean) value);
                    else if (value instanceof Float) stateEdit.putFloat(key, (Float) value);
                    else if (value instanceof Integer) stateEdit.putInt(key, (Integer) value);
                    else if (value instanceof Long) stateEdit.putLong(key, (Long) value);
                    else if (value instanceof Set<?>)
                    {
                        try
                        {
                            //noinspection unchecked
                            stateEdit.putStringSet(key, (Set<String>) value);
                        }
                        catch (ClassCastException e)
                        {
                            Log.wtf(LOGGER_TAG,
                                    "Set value isn't a string set even though shared preferences " +
                                    "only support string sets!", e);
                        }
                    }
                    else
                    {
                        Log.w(LOGGER_TAG, "Unknown value type in shared preferences file.");
                    }

                    Log.d(LOGGER_TAG, "Copied value " + key +
                                      " to new shared preferences file.");
                }

                // Finally, save the new configuration and clear the old one.
                stateEdit.commit();
                oldPrefs.edit().clear().commit();
            }

            editor.commit();
            break;
        }
    }

    /**
     * Adds the given automation's ID to the ID list.
     *
     * @param automation
     *         The automation to add.
     */
    public <T extends Automation> void addAutomation(T automation, AutomationTypeInterface<T> type)
    {
        List<T> list = type.getList();

        SharedPreferences prefs = getSharedPreferences(PREF_AUTOMATIONS, MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();

        Set<String> automationIDs = new HashSet<String>();
        automationIDs.add(((Integer) automation.getId()).toString());
        automationIDs.addAll(prefs.getStringSet(type.getIdListKey(), new HashSet<String>()));

        edit.putStringSet(type.getIdListKey(), automationIDs);
        list.add(automation);
        automation.create();
        boolean success = edit.commit();
        if (!success) Log.e(LOGGER_TAG, "Failed to commit changes to preferences.");
        else onAutomationListChange();

        assert prefs.getStringSet(type.getIdListKey(), new HashSet<String>()).equals(automationIDs);
    }

    private void deleteAutomation(int id, AutomationTypeInterface type)
    {
        Automation automation = type.findById(id);
        if (automation == null)
        {
            Log.e(LOGGER_TAG,
                  "Attempted to delete a " + type.getTypeName(false) + " that doesn't exist.");
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREF_AUTOMATIONS, MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();

        Set<String> automationIDs = new HashSet<String>();
        automationIDs.addAll(prefs.getStringSet(type.getIdListKey(), new HashSet<String>()));
        automationIDs.remove(((Integer) id).toString());

        edit.putStringSet(type.getIdListKey(), automationIDs);

        // Clear the component's preferences.
        getSharedPreferences(automation.getSharedPreferencesName(), MODE_PRIVATE).edit().clear()
                .commit();

        automation.destroy();
        type.getList().remove(automation);
        boolean success = edit.commit();
        if (!success) Log.e(LOGGER_TAG, "Failed to commit changes to preferences.");
        else onAutomationListChange();
    }

    public int findUnusedId(AutomationTypeInterface type)
    {
        SharedPreferences prefs = getSharedPreferences(PREF_AUTOMATIONS, MODE_PRIVATE);
        Set<String> usedIds = prefs.getStringSet(type.getIdListKey(), new HashSet<String>());

        int greatestValue = 0;
        for (String stringVal : usedIds)
        {
            try
            {
                int value = Integer.parseInt(stringVal);
                if (value > greatestValue) greatestValue = value;
            }
            catch (NumberFormatException e)
            {
                Log.w(LOGGER_TAG,
                      "Found non-integer value in " + type.getTypeName(false) + " ID set.", e);
            }
        }

        // To get an unused ID, we just find an ID value greater than the greatest one.
        return greatestValue + 1;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
    {
        if (key.equals("name") || key.equals("description") || key.equals("enabled"))
            onAutomationListChange();
    }

    public class LocalBinder extends Binder
    {
        public List<State> getStateList()
        {
            return mStates;
        }

        public List<Event> getEventList()
        {
            return mEvents;
        }

        /**
         * Attempts to get the state with the given ID.
         *
         * @param id
         *         The ID of the state to find.
         *
         * @return The state with the given ID or null if no automation was found.
         */
        public State findStateById(int id)
        {
            return AutomationService.this.mStateTypeInterface.findById(id);
        }

        /**
         * Attempts to get the event with the given ID.
         *
         * @param id
         *         The ID of the event to find.
         *
         * @return The event with the given ID or null if no event was found.
         */
        public ConfigComponent findEventById(int id)
        {
            return AutomationService.this.mEventTypeInterface.findById(id);
        }

        /**
         * Creates a new automation and adds it to the automation list.
         *
         * @param name
         *         The name of the new automation.
         */
        public void createNewState(String name)
        {
            State state = StateBase.createNewState(name, AutomationService.this,
                                                   findUnusedId(mStateTypeInterface));
            addAutomation(state, mStateTypeInterface);
        }

        /**
         * Tries to delete the automation with the given ID.
         *
         * @param id
         *         The ID of the automation to delete.
         */
        public void deleteState(int id)
        {
            AutomationService.this.deleteAutomation(id, mStateTypeInterface);
        }


        /**
         * Creates a new event and adds it to the event list.
         *
         * @param name
         *         The name of the new event.
         */
        public void createNewEvent(String name)
        {
            Event event = EventBase.createNewEvent(name, AutomationService.this,
                                                   findUnusedId(mEventTypeInterface));
            AutomationService.this.addAutomation(event, mEventTypeInterface);
        }

        /**
         * Tries to delete the automation with the given ID.
         *
         * @param id
         *         The ID of the automation to delete.
         */
        public void deleteEvent(int id)
        {
            AutomationService.this.deleteAutomation(id, mEventTypeInterface);
        }


        public void registerAutomationListChangeListener(AutomationListChangeListener listener)
        {
            mAutomationListChangeListeners.add(listener);
        }

        public void unregisterAutomationListChangeListener(AutomationListChangeListener listener)
        {
            mAutomationListChangeListeners.remove(listener);
        }

        public AutomationService getService()
        {
            return AutomationService.this;
        }
    }

    public void onAutomationListChange()
    {
        for (AutomationListChangeListener listener : mAutomationListChangeListeners)
        {
            if (listener != null) listener.onAutomationListChange();
        }
    }


    private StateTypeInterface mStateTypeInterface = new StateTypeInterface();

    private EventTypeInterface mEventTypeInterface = new EventTypeInterface();


    private class StateTypeInterface implements AutomationTypeInterface<State>
    {
        @Override
        public String getTypeName(boolean upper)
        {
            return upper ? getString(R.string.state_upper) : getString(R.string.state_lower);
        }

        @Override
        public String getIdListKey()
        {
            return VALUE_STATE_IDS;
        }

        @Override
        public List<State> getList()
        {
            return mStates;
        }

        @Override
        public State loadFromPrefs(int id)
        {
            return StateBase.fromSharedPreferences(AutomationService.this, id);
        }

        @Override
        public State findById(int id)
        {
            for (State state : mStates)
                if (state.getId() == id) return state;
            return null;
        }
    }

    private class EventTypeInterface implements AutomationTypeInterface<Event>
    {
        @Override
        public String getTypeName(boolean upper)
        {
            return upper ? getString(R.string.event_upper) : getString(R.string.event_lower);
        }

        @Override
        public String getIdListKey()
        {
            return VALUE_EVENT_IDS;
        }

        @Override
        public List<Event> getList()
        {
            return mEvents;
        }

        @Override
        public Event loadFromPrefs(int id)
        {
            return EventBase.fromSharedPreferences(AutomationService.this, id);
        }

        @Override
        public Event findById(int id)
        {
            for (Event event : mEvents)
                if (event.getId() == id) return event;
            return null;
        }
    }


    /**
     * An interface for managing automation types.
     *
     * @param <T>
     */
    private interface AutomationTypeInterface<T extends Automation>
    {
        public abstract String getTypeName(boolean upper);

        public abstract String getIdListKey();

        public abstract List<T> getList();

        public abstract T loadFromPrefs(int id);

        public abstract T findById(int id);
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
