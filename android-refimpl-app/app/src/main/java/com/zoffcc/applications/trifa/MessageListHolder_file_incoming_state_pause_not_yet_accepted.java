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

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import static com.zoffcc.applications.trifa.MainActivity.get_filetransfer_filenum_from_id;
import static com.zoffcc.applications.trifa.MainActivity.set_filetransfer_accepted_from_id;
import static com.zoffcc.applications.trifa.MainActivity.set_filetransfer_state_from_id;
import static com.zoffcc.applications.trifa.MainActivity.set_message_accepted_from_id;
import static com.zoffcc.applications.trifa.MainActivity.set_message_state_from_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_file_control;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.update_single_message_from_messge_id;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME;

public class MessageListHolder_file_incoming_state_pause_not_yet_accepted extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
{
    private static final String TAG = "trifa.MessageListHolder";

    private Message message2;
    private Context context;

    ImageButton button_ok;
    ImageButton button_cancel;
    ProgressBar ft_progressbar;
    ViewGroup ft_preview_container;
    ViewGroup ft_buttons_container;
    ImageButton ft_preview_image;
    TextView textView;

    public MessageListHolder_file_incoming_state_pause_not_yet_accepted(View itemView, Context c)
    {
        super(itemView);

        Log.i(TAG, "MessageListHolder");

        this.context = c;

        button_ok = (ImageButton) itemView.findViewById(R.id.ft_button_ok);
        button_cancel = (ImageButton) itemView.findViewById(R.id.ft_button_cancel);
        ft_progressbar = (ProgressBar) itemView.findViewById(R.id.ft_progressbar);
        ft_preview_container = (ViewGroup) itemView.findViewById(R.id.ft_preview_container);
        ft_buttons_container = (ViewGroup) itemView.findViewById(R.id.ft_buttons_container);
        ft_preview_image = (ImageButton) itemView.findViewById(R.id.ft_preview_image);
        textView = (TextView) itemView.findViewById(R.id.m_text);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public void bindMessageList(Message m)
    {
        Log.i(TAG, "bindMessageList");

        if (m == null)
        {
            // TODO: should never be null!!
            // only afer a crash
            m = new Message();
        }

        final Message message = m;

        final Drawable d1 = new IconicsDrawable(context).
                icon(GoogleMaterial.Icon.gmd_check_circle).
                backgroundColor(Color.TRANSPARENT).
                color(Color.parseColor("#EF088A29")).sizeDp(50);
        button_ok.setImageDrawable(d1);
        final Drawable d2 = new IconicsDrawable(context).
                icon(GoogleMaterial.Icon.gmd_highlight_off).
                backgroundColor(Color.TRANSPARENT).
                color(Color.parseColor("#A0FF0000")).sizeDp(50);
        button_cancel.setImageDrawable(d2);
        ft_buttons_container.setVisibility(View.VISIBLE);

        button_ok.setVisibility(View.VISIBLE);
        button_cancel.setVisibility(View.VISIBLE);

        // TODO: make text better
        textView.setText("" + message.text + "\n Accept File?");

        ft_progressbar.setIndeterminate(true);
        ft_progressbar.setVisibility(View.VISIBLE);

        button_ok.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    try
                    {
                        // accept FT
                        set_filetransfer_accepted_from_id(message.filetransfer_id);
                        set_filetransfer_state_from_id(message.filetransfer_id, TOX_FILE_CONTROL_RESUME.value);
                        set_message_accepted_from_id(message.id);
                        set_message_state_from_id(message.id, TOX_FILE_CONTROL_RESUME.value);
                        tox_file_control(tox_friend_by_public_key__wrapper(message.tox_friendpubkey), get_filetransfer_filenum_from_id(message.filetransfer_id), TOX_FILE_CONTROL_RESUME.value);

                        ft_progressbar.setProgress(0);
                        ft_progressbar.setMax(100);
                        ft_progressbar.setIndeterminate(true);
                        ft_progressbar.setVisibility(View.VISIBLE);
                        button_ok.setVisibility(View.GONE);

                        // update message view
                        update_single_message_from_messge_id(message.id, true);

                        Log.i(TAG, "button_ok:OnTouch:009");
                    }
                    catch (Exception e)
                    {
                    }
                }
                else
                {
                }
                return true;
            }
        });


        button_cancel.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    try
                    {
                        // cancel FT
                        Log.i(TAG, "button_cancel:OnTouch:001");
                        // values.get(position).state = TOX_FILE_CONTROL_CANCEL.value;
                        tox_file_control(tox_friend_by_public_key__wrapper(message.tox_friendpubkey), get_filetransfer_filenum_from_id(message.filetransfer_id), TOX_FILE_CONTROL_CANCEL.value);
                        set_filetransfer_state_from_id(message.filetransfer_id, TOX_FILE_CONTROL_CANCEL.value);
                        set_message_state_from_id(message.id, TOX_FILE_CONTROL_CANCEL.value);

                        button_ok.setVisibility(View.GONE);
                        button_cancel.setVisibility(View.GONE);
                        ft_progressbar.setVisibility(View.GONE);

                        // update message view
                        update_single_message_from_messge_id(message.id, true);
                    }
                    catch (Exception e)
                    {
                    }
                }
                else
                {
                }
                return true;
            }
        });

    }

    @Override
    public void onClick(View v)
    {
        Log.i(TAG, "onClick");
        try
        {
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onClick:EE:" + e.getMessage());
        }
    }

    @Override
    public boolean onLongClick(final View v)
    {
        Log.i(TAG, "onLongClick");

        // final Message m2 = this.message;

        return true;
    }
}
