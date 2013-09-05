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

package net.forkk.autocron.data.trigger;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import net.forkk.autocron.data.Automation;
import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.ComponentType;

import java.util.ArrayList;


/**
 * Component type for triggers.
 */
public class TriggerType extends ComponentType<Trigger>
{
    /**
     * @return The next available trigger ID.
     */
    @Override
    public int getAvailableId(Context context)
    {
        return context.getSharedPreferences(AutomationService.PREF_AUTOMATIONS,
                                            Context.MODE_PRIVATE).getInt("next_trigger_id", 0);
    }

    @Override
    public void incrementNextId(Context context)
    {
        SharedPreferences.Editor edit =
                context.getSharedPreferences(AutomationService.PREF_AUTOMATIONS,
                                             Context.MODE_PRIVATE).edit();
        edit.putInt("next_trigger_id", getAvailableId(context) + 1);
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
    public TriggerType(String typeName, String typeDesc, Class<? extends Trigger> typeClass)
    {
        super(typeName, typeDesc, typeClass);
    }

    public static TriggerType[] getTriggerTypes()
    {
        return TRIGGER_TYPES;
    }

    // Damn IntelliJ and its shitty code formatting...
    private static TriggerType[] TRIGGER_TYPES;

    public static void initialize(Context context)
    {
        Resources res = context.getResources();
        assert res != null;

        ArrayList<TriggerType> types = new ArrayList<TriggerType>();

        types.add(TestTrigger.initComponentType(res));

        for (TriggerType type : types)
            type.checkIfSupported(context);

        TRIGGER_TYPES = types.toArray(new TriggerType[types.size()]);
    }

    public static Trigger fromSharedPreferences(Automation parent, Context context, int id)
    {
        SharedPreferences preferences =
                context.getSharedPreferences(TriggerBase.getSharedPreferencesNameForId(id),
                                             Context.MODE_PRIVATE);

        String typeId = preferences.getString(VALUE_COMPONENT_TYPE, null);
        if (typeId == null) return null;
        else
        {
            // Find a TriggerType whose typeID matches.
            for (TriggerType type : getTriggerTypes())
                if (typeId.equals(type.getTypeId())) return type.construct(parent, context, id);
            return null;
        }
    }
}
