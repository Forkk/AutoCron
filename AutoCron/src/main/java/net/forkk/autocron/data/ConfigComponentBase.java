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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceFragment;


/**
 * Abstract base for all config components.
 */
public abstract class ConfigComponentBase
        implements ConfigComponent, SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String VALUE_NAME = "name";

    public static final String VALUE_DESCRIPTION = "description";

    public static final String VALUE_ENABLED = "enabled";

    private SharedPreferences mPreferences;

    protected AutomationService mService;

    private int mId;

    private boolean mIsEnableStateChanging = false;

    public ConfigComponentBase(AutomationService service, int id)
    {
        mId = id;
        mService = service;
        mPreferences =
                service.getSharedPreferences(getSharedPreferencesName(id), Context.MODE_PRIVATE);
    }

    @Override
    public void create()
    {
        if (isEnabled()) onCreate();
    }

    @Override
    public void destroy()
    {
        if (isEnabled()) onDestroy();
    }

    protected AutomationService getService()
    {
        return mService;
    }

    /**
     * Called after the automation service finishes loading components. This should perform all
     * necessary initialization for this component.
     */
    protected abstract void onCreate();

    /**
     * Called when the automation service is destroyed. This should perform all necessary cleanup.
     */
    protected abstract void onDestroy();

    /**
     * Gets this component's shared preferences name for the given component ID.
     *
     * @param id
     *         The component's ID.
     *
     * @return The shared preferences ID for the corresponding ID.
     */
    protected abstract String getSharedPreferencesName(int id);

    /**
     * Gets this component's shared preferences name.
     *
     * @return The shared preferences name for this component.
     */
    @Override
    public String getSharedPreferencesName()
    {
        return getSharedPreferencesName(getId());
    }

    protected SharedPreferences getSharedPreferences()
    {
        return mPreferences;
    }

    @Override
    public int getId()
    {
        return mId;
    }

    /**
     * @return The user-given name for this component.
     */
    @Override
    public String getName()
    {
        return getSharedPreferences().getString(VALUE_NAME, "Unnamed");
    }

    /**
     * @return The user-given description for this component.
     */
    @Override
    public String getDescription()
    {
        return getSharedPreferences().getString(VALUE_DESCRIPTION, "");
    }

    @Override
    public boolean isEnabled()
    {
        return mIsEnableStateChanging || getSharedPreferences().getBoolean(VALUE_ENABLED, false);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        // HACK: To fix some issues, components are always enabled while its enabled state is changing.
        mIsEnableStateChanging = true;
        if (enabled) create();
        else destroy();
        getSharedPreferences().edit().putBoolean(VALUE_ENABLED, enabled).commit();
        mIsEnableStateChanging = false;
    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {

    }

    /**
     * @return The Resources object.
     */
    public Resources getResources()
    {
        return getService().getResources();
    }
}
