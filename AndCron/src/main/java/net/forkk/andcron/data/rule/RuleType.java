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

package net.forkk.andcron.data.rule;

import android.content.Context;
import android.content.SharedPreferences;

import net.forkk.andcron.data.AutomationService;
import net.forkk.andcron.data.ComponentType;


/**
 * ComponentType for rules.
 */
public class RuleType extends ComponentType<Rule>
{
    /**
     * @return The next available rule ID.
     */
    @Override
    public int getAvailableId(Context context)
    {
        return context.getSharedPreferences(AutomationService.PREF_AUTOMATIONS,
                                            Context.MODE_PRIVATE).getInt("next_rule_id", 0);
    }

    @Override
    public void incrementNextId(Context context)
    {
        SharedPreferences.Editor edit =
                context.getSharedPreferences(AutomationService.PREF_AUTOMATIONS,
                                             Context.MODE_PRIVATE).edit();
        edit.putInt("next_rule_id", getAvailableId(context) + 1);
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
    public RuleType(String typeName, String typeDesc, Class<? extends Rule> typeClass)
    {
        super(typeName, typeDesc, typeClass);
    }

    public static RuleType[] getRuleTypes()
    {
        return RULE_TYPES;
    }

    // Damn IntelliJ and its shitty code formatting...
    private static final RuleType[] RULE_TYPES = new RuleType[] {
                                                                        new RuleType("Test Rule",
                                                                                     "A simple rule for testing.",
                                                                                     TestRule.class),
    };

    public static Rule fromSharedPreferences(Context context, int id)
    {
        SharedPreferences preferences =
                context.getSharedPreferences(RuleBase.getSharedPreferencesNameForId(id),
                                             Context.MODE_PRIVATE);

        String typeId = preferences.getString(VALUE_COMPONENT_TYPE, null);
        if (typeId == null) return null;
        else
        {
            // Find a RuleType whose typeID matches.
            for (RuleType type : getRuleTypes())
                if (typeId.equals(type.getTypeId())) return type.construct(context, id);
            return null;
        }
    }
}
