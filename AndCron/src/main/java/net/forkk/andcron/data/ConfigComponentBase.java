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

import android.content.Context;
import android.content.SharedPreferences;
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

    public ConfigComponentBase(AutomationService service, int id)
    {
        mId = id;
        mService = service;
        mPreferences =
                service.getSharedPreferences(getSharedPreferencesName(id), Context.MODE_PRIVATE);
    }

    @Override
    public void create(AutomationService service)
    {
        if (isEnabled()) onCreate(service);
    }

    @Override
    public void destroy(AutomationService service)
    {
        if (isEnabled()) onDestroy(service);
    }

    protected AutomationService getService()
    {
        return mService;
    }

    /**
     * Called after the automation service finishes loading components. This should perform all
     * necessary initialization for this component.
     *
     * @param service
     *         The automation service that this component belongs to.
     */
    protected abstract void onCreate(AutomationService service);

    /**
     * Called when the automation service is destroyed. This should perform all necessary cleanup.
     *
     * @param service
     *         The automation service that this component belongs to.
     */
    protected abstract void onDestroy(AutomationService service);

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
        return getSharedPreferences().getBoolean(VALUE_ENABLED, false);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        getSharedPreferences().edit().putBoolean(VALUE_ENABLED, enabled).commit();
        if (enabled) onCreate(getService());
        else onDestroy(getService());
    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {

    }
}
