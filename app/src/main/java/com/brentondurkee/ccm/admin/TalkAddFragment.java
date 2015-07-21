/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee.
 */

package com.brentondurkee.ccm.admin;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.provider.SyncPosts;
import com.brentondurkee.ccm.provider.SyncUtil;

/**
    Fragment activity for the add talk activity
 */
public class TalkAddFragment extends Fragment {

    public TalkAddFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        final View rootView = inflater.inflate(R.layout.fragment_add_talk, container, false);

        //puts all the data into the bundle, splitting the outline
        rootView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle data = new Bundle();
                data.putString(SyncPosts.TALK_AUTHOR, ((EditText) rootView.findViewById(R.id.talkAddAuthor)).getText().toString());
                data.putString(SyncPosts.TALK_SUBJECT, ((EditText) rootView.findViewById(R.id.talkAddTopic)).getText().toString());
                data.putString(SyncPosts.TALK_DATE, ((EditText) rootView.findViewById(R.id.talkAddTime)).getText().toString());
                data.putString(SyncPosts.TALK_REFERENCE, ((EditText) rootView.findViewById(R.id.talkAddVerse)).getText().toString());
                String outline = ((EditText) rootView.findViewById(R.id.talkAddOutline)).getText().toString();
                data.putStringArray(SyncPosts.TALK_OUTLINE, outline.split("\\n"));

                //runs the network io in a seperate thread
                new AsyncTask<Bundle, Void, Boolean>(){
                    @Override
                    protected Boolean doInBackground(Bundle... data) {
                        return SyncPosts.addTalk(data[0], SyncUtil.getAccount(), getActivity());
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        super.onPostExecute(aBoolean);
                        if (aBoolean) {
                            AdminUtil.toast(getActivity(), "Talk Added Successfully");
                            AdminUtil.succeed(getActivity());
                        } else {
                            AdminUtil.toast(getActivity(), "Failed to Add Talk");
                        }
                    }
                }.execute(data);


            }
        });

        return rootView;
    }
}