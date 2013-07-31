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

import net.forkk.andcron.data.ComponentType;


/**
 * ComponentType for actions.
 */
public class ActionType extends ComponentType<Action>
{

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

    public static ActionType[] getRuleTypes()
    {
        return ACTION_TYPES;
    }

    // Damn IntelliJ and its shitty code formatting...
    private static final ActionType[] ACTION_TYPES = new ActionType[] {
                                                                              new ActionType("Test Action",
                                                                                             "A simple action for testing.",
                                                                                             TestAction
                                                                                                     .class),
    };
}
