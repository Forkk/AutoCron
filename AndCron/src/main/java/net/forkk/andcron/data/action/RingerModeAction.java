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

package net.forkk.andcron.data.action;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.preference.PreferenceFragment;

import net.forkk.andcron.R;
import net.forkk.andcron.data.Automation;
import net.forkk.andcron.data.AutomationService;
import net.forkk.andcron.data.ComponentType;


/**
 * An action that changes the device's ringer mode.
 */
public class RingerModeAction extends ActionBase
{
    private static ActionType sComponentType;

    public static ActionType initComponentType(Resources res)
    {
        return sComponentType = new ActionType(res.getString(R.string.ringer_mode_action_title),
                                               res.getString(R.string.ringer_mode_action_description),
                                               RingerModeAction.class);
    }

    public static ActionType getComponentType()
    {
        return sComponentType;
    }

    public RingerModeAction(Automation parent, AutomationService service, int id)
    {
        super(parent, service, id);
    }

    @Override
    public void onCreate(AutomationService service)
    {

    }

    @Override
    public void onDestroy(AutomationService service)
    {

    }

    @Override
    public void onActivate(AutomationService service)
    {
        changeMode(service, getSharedPreferences().getString("activate_mode", "none"));
    }

    @Override
    public void onDeactivate(AutomationService service)
    {
        changeMode(service, getSharedPreferences().getString("deactivate_mode", "none"));
    }

    public void changeMode(Context context, String mode)
    {
        if (!mode.equals("none"))
        {
            AudioManager audioManager =
                    (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            if (mode.equals("normal")) audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            else if (mode.equals("vibrate"))
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            else if (mode.equals("silent"))
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_ringer_mode_action);
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
}
