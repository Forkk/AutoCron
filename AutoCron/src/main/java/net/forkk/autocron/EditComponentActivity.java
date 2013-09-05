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

package net.forkk.autocron;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import net.forkk.autocron.data.ComponentPointer;
import net.forkk.autocron.data.action.Action;
import net.forkk.autocron.data.rule.Rule;
import net.forkk.autocron.data.trigger.Trigger;


public class EditComponentActivity extends FragmentActivity
{
    public static final String EXTRA_COMPONENT_POINTER = "net.forkk.autocron.component_ptr";

    public EditComponentActivity()
    {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_component);

        Intent intent = getIntent();
        assert intent != null;

        ComponentPointer pointer =
                (ComponentPointer) intent.getSerializableExtra(EXTRA_COMPONENT_POINTER);

        // Hack to get the title to display properly.
        String componentName = "";
        if (pointer instanceof Rule.Pointer) componentName = getString(R.string.rule_upper);
        if (pointer instanceof Action.Pointer) componentName = getString(R.string.action_upper);
        if (pointer instanceof Trigger.Pointer) componentName = getString(R.string.trigger_upper);
        setTitle(getString(R.string.title_activity_edit_component, componentName));

        if (savedInstanceState == null)
        {
            Bundle fragArgs = new Bundle();
            fragArgs.putSerializable(ComponentPreferenceFragment.VALUE_COMPONENT_POINTER, pointer);

            ComponentPreferenceFragment fragment = new ComponentPreferenceFragment();
            fragment.setArguments(fragArgs);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_edit_component_container, fragment);
            transaction.commit();
        }
    }
}
