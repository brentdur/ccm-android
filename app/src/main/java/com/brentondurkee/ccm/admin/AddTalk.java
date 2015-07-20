/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee.
 */

package com.brentondurkee.ccm.admin;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toolbar;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.provider.SyncPosts;
import com.brentondurkee.ccm.provider.SyncUtil;

/**
    Fragment activity for the add talk activity
 */
public class AddTalk extends FragmentActivity {
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //sets up the toolbar
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.primaryCCM));
        toolbar.setTitleTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
        setActionBar(toolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new TalkAddFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_talk, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class TalkAddFragment extends Fragment {

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
}
