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
import android.content.SharedPreferences;
import android.content.res.Resources;

import net.forkk.andcron.R;
import net.forkk.andcron.data.Automation;
import net.forkk.andcron.data.AutomationService;
import net.forkk.andcron.data.ComponentType;

import java.util.ArrayList;


/**
 * ComponentType for actions.
 */
public class ActionType extends ComponentType<Action>
{
    /**
     * @return The next available rule ID.
     */
    @Override
    public int getAvailableId(Context context)
    {
        return context.getSharedPreferences(AutomationService.PREF_AUTOMATIONS,
                                            Context.MODE_PRIVATE).getInt("next_action_id", 0);
    }

    @Override
    public void incrementNextId(Context context)
    {
        SharedPreferences.Editor edit =
                context.getSharedPreferences(AutomationService.PREF_AUTOMATIONS,
                                             Context.MODE_PRIVATE).edit();
        edit.putInt("next_action_id", getAvailableId(context) + 1);
        edit.commit();
    }

    /**
     * Constructs a new component type.
     *
     * @param typeName
     *         The type's name.
     * @param typeDesc
     *         The type's description.
     * @param typeClass
     *         The type's class.
     */
    public ActionType(String typeName, String typeDesc, Class<? extends Action> typeClass)
    {
        super(typeName, typeDesc, typeClass);
    }

    public static ActionType[] getActionTypes()
    {
        return ACTION_TYPES;
    }

    // Damn IntelliJ and its shitty code formatting...
    private static ActionType[] ACTION_TYPES;

    public static void initialize(Context context)
    {
        Resources res = context.getResources();
        assert res != null;

        ArrayList<ActionType> types = new ArrayList<ActionType>();

        types.add(new ActionType(res.getString(R.string.ringer_mode_action_title),
                                 res.getString(R.string.ringer_mode_action_description),
                                 RingerModeAction.class));

        // TODO: Disable the send SMS action when the device doesn't support SMS.
        types.add(new ActionType(res.getString(R.string.send_sms_action_title),
                                 res.getString(R.string.send_sms_action_description),
                                 SendSMSAction.class));

        types.add(new ActionType(res.getString(R.string.test_action_title),
                                 res.getString(R.string.test_action_description),
                                 TestAction.class));

        ACTION_TYPES = types.toArray(new ActionType[types.size()]);
    }


    public static Action fromSharedPreferences(Automation parent, Context context, int id)
    {
        SharedPreferences preferences =
                context.getSharedPreferences(ActionBase.getSharedPreferencesNameForId(id),
                                             Context.MODE_PRIVATE);

        String typeId = preferences.getString(VALUE_COMPONENT_TYPE, null);
        if (typeId == null) return null;
        else
        {
            // Find an ActionType whose typeID matches.
            for (ActionType type : getActionTypes())
                if (typeId.equals(type.getTypeId())) return type.construct(parent, context, id);
            return null;
        }
    }
}
