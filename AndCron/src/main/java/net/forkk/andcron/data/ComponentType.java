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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * Represents a specific type component and info about it.
 */
public abstract class ComponentType<T extends AutomationComponent>
{
    private String mTypeName;

    private String mTypeDesc;

    protected Class<? extends T> mTypeClass;

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
    public ComponentType(String typeName, String typeDesc, Class<? extends T> typeClass)
    {
        mTypeName = typeName;
        mTypeDesc = typeDesc;
        mTypeClass = typeClass;
    }

    /**
     * Creates a new action of this type with the given name and description.
     *
     * @return The new action.
     */
    public T createNew()
            throws NoSuchMethodException
    {
        Constructor<? extends T> constructor = mTypeClass.getConstructor();
        try
        {
            return constructor.newInstance();
        }
        catch (InstantiationException e)
        {
            // TODO: Handle these errors properly.
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String getTypeName()
    {
        return mTypeName;
    }

    public String getTypeDesc()
    {
        return mTypeDesc;
    }
}
