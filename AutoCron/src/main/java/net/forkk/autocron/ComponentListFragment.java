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

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import net.forkk.autocron.data.ConfigComponent;

import java.util.List;


/**
 * List fragment for listing components.
 */
public abstract class ComponentListFragment extends ListFragment
{
    protected ComponentListAdapter mAdapter;

    private ContextMenu.ContextMenuInfo mMenuInfo;

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

    protected abstract void onEditComponent(long id);

    protected abstract void onDeleteComponent(int id);

    protected abstract void onActionAddComponent();

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
        onEditComponent(id);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.component_list_menu, menu);
        MenuItem addItem = menu.findItem(R.id.action_add_component);

        assert addItem != null;
        addItem.setTitle(getResources().getString(R.string.action_add_component,
                                                  getComponentTypeName(false)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.action_add_component:
            onActionAddComponent();
            return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo)
    {
        mMenuInfo = menuInfo;
        menu.setHeaderTitle(getResources().getString(R.string.title_component_context_menu,
                                                     getComponentTypeName(true)));
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.component_context_menu, menu);

        MenuItem editItem = menu.findItem(R.id.action_edit_component);
        MenuItem deleteItem = menu.findItem(R.id.action_delete_component);

        assert editItem != null;
        editItem.setTitle(getResources().getString(R.string.action_edit_component,
                                                   getComponentTypeName(false)));
        assert deleteItem != null;
        deleteItem.setTitle(getResources().getString(R.string.action_delete_component,
                                                     getComponentTypeName(false)));
        super.onCreateContextMenu(menu, view, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if (item.getMenuInfo() != mMenuInfo) return false;

        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        assert info != null;
        final ConfigComponent component = findComponentById((int) info.id);
        assert component != null;

        switch (item.getItemId())
        {
        case R.id.action_edit_component:
            onEditComponent(info.id);
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
                final ConfigComponent component = getComponentList().get(i);

                view = mInflater.inflate(R.layout.component_list_item, null);

                assert view != null;
                TextView itemNameView = (TextView) view.findViewById(R.id.component_name_view);
                TextView itemDescView = (TextView) view.findViewById(R.id.component_desc_view);
                Switch enableSwitch = (Switch) view.findViewById(R.id.component_enable_switch);

                itemNameView.setText(component.getName());

                String description = component.getDescription();
                if (description.isEmpty()) itemDescView.setText(getResources()
                                                                        .getString(R.string.component_no_description));
                else itemDescView.setText(description);

                enableSwitch.setChecked(component.isEnabled());
                enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked)
                    {
                        component.setEnabled(checked);
                    }
                });

                return view;
            }
            else return null;
        }
    }
}
