/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
 */

package com.brentondurkee.ccm.inbox;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.provider.DataContract;

/**
 * Created by brenton on 8/22/15.
 */
public class CommunicationsCursorAdapter extends CursorAdapter {

    public CommunicationsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.message, parent, false);

        MessageViewHolder holder = new MessageViewHolder();
        holder.subjectView = (TextView) v.findViewById(R.id.msgSubject);
        holder.fromView = (TextView) v.findViewById(R.id.msgFrom);

        v.setTag(holder);

        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final MessageViewHolder holder = (MessageViewHolder) view.getTag();
        if (cursor.getColumnName(1).equals(DataContract.Convo.COLUMN_NAME_SUBJECT)){
            holder.subject = cursor.getString(1);
            holder.from = cursor.getString(2);
        }
        else {
            holder.subject = cursor.getString(1);
            holder.from = "Broadcast";
        }

        holder.subjectView.setText(holder.subject);
        if(holder.from.isEmpty()){
            holder.from = "Anonymous";
        }
        holder.fromView.setText(holder.from);
    }

    private static class MessageViewHolder {
        public TextView subjectView;
        public TextView fromView;
        public String subject;
        public String from;
        public String date;

    }
}


