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

package net.forkk.andcron;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.forkk.andcron.data.ConfigComponent;

import java.util.List;


/**
 * List fragment for listing components.
 */
public abstract class ComponentListFragment extends ListFragment
{
    protected ComponentListAdapter mAdapter;

    @Override
    public void onStart()
    {
        super.onStart();
        registerForContextMenu(getListView());
        setEmptyText(getResources().getString(R.string.component_list_empty_text,
                                              getComponentTypeName(false)));
    }

    /**
     * @return An array of the components that this list should contain.
     */
    protected abstract List<ConfigComponent> getComponentList();

    /**
     * @return Whether or not there are items in the list.
     */
    protected abstract boolean hasItems();

    /**
     * Finds the component with the given ID.
     */
    protected abstract ConfigComponent findComponentById(int id);

    protected abstract void onEditComponent(int position, long id);

    protected abstract void onDeleteComponent(int id);

    protected abstract void onAddComponent(String name);

    public abstract String getComponentTypeName(boolean upper);

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mAdapter = new ComponentListAdapter(getActivity());
        setListAdapter(mAdapter);

        setHasOptionsMenu(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        onEditComponent(position, id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.action_add_component:
            final View inputView =
                    getActivity().getLayoutInflater().inflate(R.layout.text_entry_view, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.title_new_component,
                                                      getComponentTypeName(true)));
            builder.setMessage(getResources().getString(R.string.message_new_component,
                                                        getComponentTypeName(false)));
            builder.setView(inputView);
            builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    dialogInterface.dismiss();
                    assert inputView != null;
                    EditText input = (EditText) inputView.findViewById(R.id.text_input);
                    //noinspection ConstantConditions
                    final String name = input.getText().toString();

                    // Add a new component with the given name.
                    onAddComponent(name);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    dialogInterface.cancel();
                }
            });
            builder.create().show();
            return true;
        }
        return false;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        assert info != null;
        final ConfigComponent component = findComponentById((int) info.id);
        assert component != null;

        switch (item.getItemId())
        {
        case R.id.action_edit_component:
            onEditComponent(info.position, info.id);
            return true;

        case R.id.action_delete_component:
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.title_confirm_delete_component));
            builder.setMessage(getResources().getString(R.string.message_confirm_delete_component,
                                                        getComponentTypeName(false),
                                                        component.getName()));
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    ComponentListFragment.this.onDeleteComponent(component.getId());
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    // Nothing to do here.
                    dialogInterface.dismiss();
                }
            });
            builder.create().show();
            return true;
        }
        return false;
    }

    public class ComponentListAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        public ComponentListAdapter(Context parent)
        {
            assert parent != null;
            assert parent.getApplicationContext() != null;

            mInflater = LayoutInflater.from(parent);
        }

        @Override
        public int getCount()
        {
            if (hasItems()) return getComponentList().size();
            else return 0;
        }

        @Override
        public Object getItem(int i)
        {
            if (hasItems()) return getComponentList().get(i);
            else return null;
        }

        @Override
        public long getItemId(int i)
        {
            if (hasItems()) return getComponentList().get(i).getId();
            else return -1;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            if (hasItems())
            {
                ConfigComponent component = getComponentList().get(i);

                view = mInflater.inflate(R.layout.component_list_item, null);

                assert view != null;
                TextView itemNameView = (TextView) view.findViewById(R.id.component_name_view);
                TextView itemDescView = (TextView) view.findViewById(R.id.component_desc_view);

                itemNameView.setText(component.getName());

                String description = component.getDescription();
                if (description.isEmpty()) itemDescView.setText(getActivity()
                                                                        .getString(R.string.component_no_description));
                else itemDescView.setText(description);

                return view;
            }
            else return null;
        }
    }
}
