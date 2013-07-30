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
import android.widget.Toast;


/**
 * Test action that shows a toast when activated or deactivated.
 */
public class TestAction extends ActionBase
{
    private Context mContext;

    /**
     * Called when the action's automation has been activated. This should perform whatever this
     * action is meant to do on activation.
     */
    @Override
    public void onActivate()
    {
        assert mContext != null;
        Toast.makeText(mContext, "Test action " + getName() + " was activated.", Toast.LENGTH_SHORT)
             .show();
    }

    /**
     * Called when the action's automation deactivates.
     */
    @Override
    public void onDeactivate()
    {
        assert mContext != null;
        Toast.makeText(mContext, "Test action " + getName() + " was deactivated.",
                       Toast.LENGTH_SHORT).show();
    }

    /**
     * Called after the automation service finishes loading components. This should perform all
     * necessary initialization for this component.
     *
     * @param context
     *         Context to initialize with.
     */
    @Override
    public void onCreate(Context context)
    {
        mContext = context;
    }

    /**
     * Called when the automation service is destroyed. This should perform all necessary cleanup.
     */
    @Override
    public void onDestroy()
    {
        mContext = null;
    }
}
