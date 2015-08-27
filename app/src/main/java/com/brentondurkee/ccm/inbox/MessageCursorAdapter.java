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
import com.brentondurkee.ccm.Utils;

/**
 * Created by brenton on 8/22/15.
 */
public class MessageCursorAdapter extends CursorAdapter {

    private int[] mCellStates;
    private String[] topicIds = new String[0];
    private String[] topicNames = new String[0];

    private static final int STATE_UNKNOWN = 0;
    private static final int STATE_SECTIONED = 1;
    private static final int STATE_REGULAR = 2;

    public MessageCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mCellStates = c == null ? null: new int[c.getCount()];
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        mCellStates = cursor == null ? null : new int[cursor.getCount()];
    }

    public void setTopicCursor(Cursor cursor) {
        topicIds = new String[cursor.getCount()];
        topicNames = new String[cursor.getCount()];
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++){
            topicIds[i] = cursor.getString(1);
            topicNames[i] = cursor.getString(2);
            cursor.moveToNext();
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.message, parent, false);

        MessageViewHolder holder = new MessageViewHolder();
        holder.seperator = (TextView) v.findViewById(R.id.separator);
        holder.subjectView = (TextView) v.findViewById(R.id.msgSubject);
        holder.fromView = (TextView) v.findViewById(R.id.msgFrom);
        holder.dateView = (TextView) v.findViewById(R.id.msgTime);

        v.setTag(holder);

        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final MessageViewHolder holder = (MessageViewHolder) view.getTag();

        boolean needSeperator = false;
        final int position = cursor.getPosition();

        holder.subject = cursor.getString(2);
        holder.from = cursor.getString(1);
        holder.date = cursor.getString(3);

        switch (mCellStates[position]){
            case STATE_SECTIONED:
                needSeperator = true;
                break;
            case STATE_REGULAR:
                needSeperator = false;
                break;
            case STATE_UNKNOWN:
            default:
                if (position == 0){
                    needSeperator = true;
                } else {
                    String currentTopic = cursor.getString(5);
                    cursor.moveToPosition(position - 1);
                    if (!cursor.getString(5).equals(currentTopic) ){
                        needSeperator = true;
                    }

                    cursor.moveToPosition(position);

                }
                mCellStates[position] = needSeperator ? STATE_SECTIONED : STATE_REGULAR;
                break;
        }

        if(needSeperator) {
            String name = "";
            for(int i = 0; i < topicIds.length; i++){
                if (topicIds[i].equals(cursor.getString(5))){
                    name = topicNames[i];
                    break;
                }
            }
            holder.seperator.setText(name);
            holder.seperator.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = holder.seperator.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.seperator.setLayoutParams(params);
        }
        else {
            ViewGroup.LayoutParams params = holder.seperator.getLayoutParams();
            params.height = 0;
            holder.seperator.setLayoutParams(params);
            holder.seperator.setVisibility(View.INVISIBLE);
        }

        holder.subjectView.setText(holder.subject);
        if(holder.from.isEmpty()){
            holder.from = "Anonymous";
        }
        holder.fromView.setText(holder.from);
        holder.date = Utils.dateForm(holder.date);
        holder.dateView.setText(holder.date);

    }

    private static class MessageViewHolder {
        public TextView seperator;
        public TextView subjectView;
        public TextView fromView;
        public TextView dateView;
        public String subject;
        public String from;
        public String date;

    }
}


