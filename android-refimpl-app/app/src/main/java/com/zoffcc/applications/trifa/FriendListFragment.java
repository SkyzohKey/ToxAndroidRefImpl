/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.trifa;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import static com.zoffcc.applications.trifa.FriendList.deep_copy;
import static com.zoffcc.applications.trifa.MainActivity.delete_friend;
import static com.zoffcc.applications.trifa.MainActivity.delete_friend_all_messages;
import static com.zoffcc.applications.trifa.MainActivity.main_handler_s;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_by_public_key;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_delete;
import static com.zoffcc.applications.trifa.MainActivity.update_savedata_file;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class FriendListFragment extends ListFragment
{
    private static final String TAG = "trifa.FriendListFrgnt";
    static final int MessageListActivity_ID = 2;
    List<FriendList> data_values = new ArrayList<FriendList>();
    FriendlistArrayAdapter a = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.friend_list_layout, container, false);
        MainActivity.friend_list_fragment = this;
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        try
        {
            ListView lv = getListView();
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
            {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
                {
                    final int position_ = position;
                    PopupMenu menu = new PopupMenu(v.getContext(), v);
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                    {
                        @Override
                        public boolean onMenuItemClick(MenuItem item)
                        {
                            int id = item.getItemId();
                            switch (id)
                            {
                                case R.id.item_delete:

                                    Runnable myRunnable = new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try
                                            {
                                                long friend_num_temp = data_values.get(position_).tox_friendnum;
                                                long friend_num_temp_safety = tox_friend_by_public_key(data_values.get(position_).tox_public_key_string);

                                                Log.i(TAG, "onMenuItemClick:1:fn=" + friend_num_temp + " fn_safety=" + friend_num_temp_safety);

                                                // delete friend -------
                                                Log.i(TAG, "onMenuItemClick:3");
                                                delete_friend(friend_num_temp);
                                                // delete friend -------

                                                // delete friends messages -------
                                                Log.i(TAG, "onMenuItemClick:2");
                                                delete_friend_all_messages(friend_num_temp);
                                                // delete friend  messages -------

                                                // delete friend - tox ----
                                                Log.i(TAG, "onMenuItemClick:4");
                                                if (friend_num_temp_safety > -1)
                                                {
                                                    int res = tox_friend_delete(friend_num_temp_safety);
                                                    update_savedata_file(); // save toxcore datafile (friend removed)
                                                    Log.i(TAG, "onMenuItemClick:5:res=" + res);
                                                }
                                                // delete friend - tox ----

                                                // load all friends into data list ---
                                                Log.i(TAG, "onMenuItemClick:6");
                                                add_all_friends_clear();
                                                Log.i(TAG, "onMenuItemClick:7");
                                                // load all friends into data list ---
                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    main_handler_s.post(myRunnable);

                                    break;
                            }
                            return true;
                        }
                    });
                    menu.inflate(R.menu.menu_friendlist_item);
                    menu.show();

                    return true;
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onCreateView:2:EE:" + e.getMessage());
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context)
    {
        Log.i(TAG, "onAttach(Context)");
        super.onAttach(context);
        data_values.clear();
        a = new FriendlistArrayAdapter(context, data_values);
        setListAdapter(a);

        //        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        //        {
        //            @Override
        //            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id)
        //            {
        //                Toast.makeText(getActivity(), "On long click listener", Toast.LENGTH_LONG).show();
        //                // tox_friend_delete
        //                return true;
        //            }
        //        });
    }

    @Override
    public void onAttach(Activity activity)
    {
        Log.i(TAG, "onAttach()");
        super.onAttach(activity);
        data_values.clear();
        a = new FriendlistArrayAdapter(activity, data_values);
        setListAdapter(a);
    }

    void modify_friend(final FriendList f, final long friendnum)
    {
        Log.i(TAG, "modify_friend");
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    boolean found_friend = false;
                    int size = data_values.size();
                    int i = 0;
                    for (i = 0; i < size; i++)
                    {
                        if (data_values.get(i).tox_friendnum == friendnum)
                        {
                            found_friend = true;
                            FriendList n = deep_copy(f);
                            data_values.set(i, n);
                            Log.i(TAG, "modify_friend:found friend:" + friendnum);
                            a.notifyDataSetChanged();
                        }
                    }

                    if (!found_friend)
                    {
                        add_friends(f);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        main_handler_s.post(myRunnable);
    }

    void clear_friends()
    {
        Log.i(TAG, "clear_friends");
        data_values.clear();
    }

    void add_friends_clear(final FriendList f)
    {
        Log.i(TAG, "add_friends_clear");
        data_values.clear();
        add_friends(f);
    }

    void add_all_friends_clear()
    {
        Log.i(TAG, "add_all_friends_clear");
        data_values.clear();

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    List<FriendList> fl = orma.selectFromFriendList().toList();
                    if (fl != null)
                    {
                        if (fl.size() > 0)
                        {
                            int i = 0;
                            for (i = 0; i < fl.size(); i++)
                            {
                                FriendList n = deep_copy(fl.get(i));
                                data_values.add(n);
                            }
                        }
                    }
                    a.notifyDataSetChanged();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        main_handler_s.post(myRunnable);
    }

    void add_friends(final FriendList f)
    {
        Log.i(TAG, "add_friends");
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    FriendList n = deep_copy(f);
                    data_values.add(n);
                    a.notifyDataSetChanged();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        main_handler_s.post(myRunnable);
    }

    public void set_all_friends_to_offline()
    {
        Log.i(TAG, "add_friends");
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    int i = 0;
                    for (i = 0; i < data_values.size(); i++)
                    {
                        data_values.get(i).TOX_CONNECTION = 0;
                    }
                    a.notifyDataSetChanged();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        main_handler_s.post(myRunnable);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Log.i(TAG, "onListItemClick pos=" + position + " id=" + id + " friendnum=" + data_values.get(position).tox_friendnum);

        Intent intent = new Intent(this.getActivity(), MessageListActivity.class);
        intent.putExtra("friendnum", data_values.get(position).tox_friendnum);
        startActivityForResult(intent, MessageListActivity_ID);
    }

}
