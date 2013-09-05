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
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * Represents a specific type component and info about it.
 */
public abstract class ComponentType<T extends AutomationComponent>
{
    public static final String VALUE_COMPONENT_TYPE = "component_type";

    private static final String LOGGER_TAG = AutomationService.LOGGER_TAG;

    private String mTypeName;

    private String mTypeDesc;

    protected Class<? extends T> mTypeClass;

    private String mTypeId;

    private String mSupportError;

    public abstract int getAvailableId(Context context);

    public abstract void incrementNextId(Context context);

    /**
     * Constructs a new component type. The typeID will be set from the typeClass's canonical name.
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
        mTypeId = typeClass.getCanonicalName();
        mTypeClass = typeClass;
    }

    /**
     * Constructs a new component type.
     *
     * @param typeName
     *         The type's name.
     * @param typeDesc
     *         The type's description.
     * @param typeId
     *         The ID string used to identify this type in configs.
     * @param typeClass
     *         The type's class.
     */
    public ComponentType(String typeName, String typeDesc, String typeId,
                         Class<? extends T> typeClass)
    {
        mTypeName = typeName;
        mTypeDesc = typeDesc;
        mTypeId = typeId;
        mTypeClass = typeClass;
    }

    public T createNew(Automation parent, Context context)
    {
        T component = construct(parent, context, getAvailableId(context));
        SharedPreferences.Editor edit =
                context.getSharedPreferences(component.getSharedPreferencesName(),
                                             Context.MODE_PRIVATE).edit();
        edit.clear().commit();
        edit.putString(VALUE_COMPONENT_TYPE, getTypeId());
        edit.commit();
        incrementNextId(context);
        return component;
    }

    /**
     * Calls the constructor for this component type.
     *
     * @return The new action.
     */
    public T construct(Automation parent, Context context, int id)
    {
        Constructor<? extends T> constructor;
        try
        {
            constructor =
                    mTypeClass.getConstructor(Automation.class, AutomationService.class, int.class);
        }
        catch (NoSuchMethodException e)
        {
            Log.wtf(LOGGER_TAG,
                    "No valid constructor found for component type " + getTypeName() + ".", e);
            return null;
        }

        try
        {
            return constructor.newInstance(parent, context, id);
        }
        catch (InstantiationException e)
        {
            // TODO: Handle these errors properly.
            Log.wtf(LOGGER_TAG, "Error creating component " + getTypeName() + ".", e);
        }
        catch (IllegalAccessException e)
        {
            Log.wtf(LOGGER_TAG, "Error creating component " + getTypeName() + ".", e);
        }
        catch (InvocationTargetException e)
        {
            Log.wtf(LOGGER_TAG, "Error creating component " + getTypeName() + ".", e);
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

    public String getTypeId()
    {
        return mTypeId;
    }

    public Class<? extends T> getTypeClass()
    {
        return mTypeClass;
    }

    /**
     * @return An error message explaining to the user why this component type is not supported on
     * his/her device. Empty string if no error occurred.
     */
    public String getSupportError()
    {
        return mSupportError;
    }

    public boolean isSupported()
    {
        return mSupportError == null || mSupportError.isEmpty();
    }

    protected void setSupportError(String error)
    {
        mSupportError = error;
    }

    /**
     * Checks if this component type will work with the current device configuration. If not, this
     * function will return false and the error message can be retrieved via the getSupportError()
     * function.
     *
     * @return True if this component type is supported by this device, otherwise, false.
     */
    public boolean checkIfSupported(Context context)
    {
        return true;
    }
}
