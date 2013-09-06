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
 * Service that stores and manages the states of different custom rule IDs.
 */
public class CustomRuleService extends Service
{
    Map<String, CustomRuleState> mCustomRuleStates;

    public CustomRuleService()
    {
        mCustomRuleStates = new HashMap<String, CustomRuleState>();
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
            if (!mCustomRuleStates.containsKey(id)) return false;
            else return mCustomRuleStates.get(id).getState();
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
}
