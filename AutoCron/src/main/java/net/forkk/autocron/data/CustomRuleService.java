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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Service that stores and manages the states of different custom rule IDs. Also handles custom
 * triggers.
 */
public class CustomRuleService extends Service
{
    Map<String, CustomRuleState> mCustomRuleStates;

    Map<String, CustomTriggerData> mCustomTriggers;

    public CustomRuleService()
    {
        mCustomRuleStates = new HashMap<String, CustomRuleState>();
        mCustomTriggers = new HashMap<String, CustomTriggerData>();
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

    public class LocalBinder extends Binder
    {
        //////////////////////
        //// Custom Rules ////
        //////////////////////

        public void registerCustomRuleListener(String id, CustomRuleListener listener)
        {
            CustomRuleState state;
            if (!mCustomRuleStates.containsKey(id))
            {
                // If there isn't already a state object with the given ID, create one.
                state = new CustomRuleState(id);
                mCustomRuleStates.put(id, state);
            }
            else state = mCustomRuleStates.get(id);
            assert state != null;
            state.registerListener(listener);
        }

        /**
         * Un-registers the given custom rule listener from the state with the given ID.
         *
         * @param id
         *         The ID of the state to un-register from.
         * @param listener
         *         The listener to un-register.
         */
        public void unregisterCustomRuleListener(String id, CustomRuleListener listener)
        {
            if (mCustomRuleStates.containsKey(id))
            {
                CustomRuleState state = mCustomRuleStates.get(id);
                assert state != null;
                state.unregisterListener(listener);
            }
        }

        /**
         * Sets the state of the given rule listener ID. If the new value is different from the old
         * value, this will call onCustomRuleStateChange for all of the state's listeners. If no
         * rule state object with the given ID has been created, this function will create one.
         *
         * @param id
         *         ID of the state to change.
         * @param value
         *         The new state value.
         */
        public void setState(String id, boolean value)
        {
            CustomRuleState state;
            if (!mCustomRuleStates.containsKey(id))
            {
                // If there isn't already a state object with the given ID, create one.
                state = new CustomRuleState(id);
                mCustomRuleStates.put(id, state);
            }
            else state = mCustomRuleStates.get(id);
            assert state != null;
            state.setState(value);
        }

        /**
         * Gets the state of the listener with the given ID. If no state object exists, returns
         * false.
         *
         * @param id
         *         ID of the state to check.
         *
         * @return The state value of the given state ID.
         */
        public boolean getState(String id)
        {
            return mCustomRuleStates.containsKey(id) && mCustomRuleStates.get(id).getState();
        }

        /////////////////////////
        //// Custom Triggers ////
        /////////////////////////

        /**
         * Registers the given trigger listener with the given custom trigger ID.
         *
         * @param id
         *         The custom trigger ID to listen for.
         * @param listener
         *         The listener to register.
         */
        public void registerCustomTriggerListener(String id, CustomTriggerListener listener)
        {
            CustomTriggerData triggerData;
            if (!mCustomTriggers.containsKey(id))
            {
                // If there isn't already a custom trigger object with the given ID, create one.
                triggerData = new CustomTriggerData(id);
                mCustomTriggers.put(id, triggerData);
            }
            else triggerData = mCustomTriggers.get(id);
            assert triggerData != null;
            triggerData.registerListener(listener);
        }

        /**
         * Un-registers the given trigger listener from the given trigger ID.
         *
         * @param id
         *         The custom trigger ID to unregister from.
         * @param listener
         *         The listener to un-register.
         */
        public void unregisterCustomTriggerListener(String id, CustomTriggerListener listener)
        {
            if (mCustomRuleStates.containsKey(id))
            {
                CustomTriggerData triggerData = mCustomTriggers.get(id);
                assert triggerData != null;
                triggerData.unregisterListener(listener);

                // If there aren't any more listeners registered with the trigger data object, 
                // we can remove it. We don't do this with custom rule objects because they need
                // to still hold their state data, but with triggers it's fine.
                if (triggerData.hasNoListeners()) mCustomTriggers.remove(id);
            }
        }

        /**
         * Triggers the custom trigger listeners with the given ID.
         */
        public void triggerCustomTrigger(String id)
        {
            // If I had a dollar for each time I've typed the word trigger over the past few days...
            if (mCustomTriggers.containsKey(id)) mCustomTriggers.get(id).trigger();
        }
    }

    /**
     * Class that stores information about a custom rule's state.
     */
    public class CustomRuleState
    {
        protected List<CustomRuleListener> mListeners;

        protected boolean mState;

        protected String mId;

        public CustomRuleState(String id)
        {
            this.mId = id;
            this.mListeners = new ArrayList<CustomRuleListener>();
            mState = false;
        }

        public String getId()
        {
            return mId;
        }

        public void setState(boolean state)
        {
            if (state != mState)
            {
                mState = state;
                for (CustomRuleListener listener : mListeners)
                    listener.onCustomRuleStateChange(getId(), mState);
            }
        }

        public boolean getState()
        {
            return mState;
        }

        public void registerListener(CustomRuleListener listener)
        {
            if (!mListeners.contains(listener)) mListeners.add(listener);
            listener.onCustomRuleStateChange(mId, mState);
        }

        public void unregisterListener(CustomRuleListener listener)
        {
            if (mListeners.contains(listener)) mListeners.remove(listener);
        }
    }

    /**
     * Interface for listening to changes in the state of a given custom rule ID.
     */
    public interface CustomRuleListener
    {
        /**
         * Called when the state of the custom rule with this listener's ID changes.
         * <p/>
         * This is also called when the custom rule is first added.
         *
         * @param id
         *         The ID of the custom rule that changed.
         * @param state
         *         The custom rule's new state.
         */
        public void onCustomRuleStateChange(String id, boolean state);
    }

    /**
     * Class that holds information about a certain custom trigger ID and its listeners.
     */
    public class CustomTriggerData
    {
        protected String mId;

        protected List<CustomTriggerListener> mListeners;

        public CustomTriggerData(String id)
        {
            mId = id;
            mListeners = new ArrayList<CustomTriggerListener>();
        }

        public String getId()
        {
            return mId;
        }

        /**
         * Sets off all of the listeners registered with this custom trigger data.
         */
        public void trigger()
        {
            // Trigger all the triggers' trigger triggered functions.
            for (CustomTriggerListener listener : mListeners)
                listener.onCustomTriggerTriggered(getId());
        }

        public void registerListener(CustomTriggerListener listener)
        {
            if (!mListeners.contains(listener)) mListeners.add(listener);
        }

        public void unregisterListener(CustomTriggerListener listener)
        {
            if (mListeners.contains(listener)) mListeners.remove(listener);
        }

        /**
         * @return True if this data object doesn't have any listeners. It may be garbage-collected
         * (removed from the data map) if so.
         */
        public boolean hasNoListeners()
        {
            return mListeners.isEmpty();
        }
    }

    /**
     * Interface for custom triggers.
     */
    public interface CustomTriggerListener
    {
        /**
         * Called when a custom trigger that ID that this listener is registered to is triggered.
         *
         * @param id
         *         The ID that was triggered.
         */
        public void onCustomTriggerTriggered(String id);
    }
}
