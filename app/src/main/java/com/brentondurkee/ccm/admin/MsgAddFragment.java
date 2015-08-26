/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee.
 */

package com.brentondurkee.ccm.admin;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.provider.DataContract;
import com.brentondurkee.ccm.provider.SyncPosts;
import com.brentondurkee.ccm.provider.SyncUtil;


/**
 * Add message activity for posting new messages
 */
public class MsgAddFragment extends Fragment implements AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final String[] from = {DataContract.Topic.COLUMN_NAME_NAME, DataContract.Topic.COLUMN_NAME_NAME};
    private final int[] to = {R.id.spinnerTarget, android.R.id.text1};
    private static final String[] PROJECTION = new String[]{
            DataContract.Topic._ID,
            DataContract.Topic.COLUMN_NAME_NAME,
            DataContract.Topic.COLUMN_NAME_ENTRY_ID
    };

    SimpleCursorAdapter mAdapter;
    Spinner spin;
    String toSelect = "";


    public MsgAddFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //loads recieving groups from cursor
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.spinner_layout, null, from, to, 0);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getLoaderManager().initLoader(0, null, this);

        final View rootView = inflater.inflate(R.layout.fragment_add_msg, container, false);
        spin = (Spinner) rootView.findViewById(R.id.spinner_to);
        spin.setAdapter(mAdapter);
        spin.setOnItemSelectedListener(this);

        rootView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle data = new Bundle();
                data.putString(SyncPosts.MSG_SUBJECT, ((EditText) rootView.findViewById(R.id.msgAddSubject)).getText().toString());
                data.putString(SyncPosts.MSG_MESSAGE, ((EditText) rootView.findViewById(R.id.msgAddMsg)).getText().toString());

                if(toSelect.equals("")){
                    AdminUtil.toast(getActivity(), "You didn't select a topic!");
                }
                else {
                    data.putString(SyncPosts.MSG_TOPIC, toSelect);
                }

                AdminUtil.showDialog(getActivity());

                new AsyncTask<Bundle, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Bundle... data) {
                        return SyncPosts.addMsg(data[0], SyncUtil.getAccount(), getActivity());
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        super.onPostExecute(aBoolean);
                        AdminUtil.hideDialog();
                        if (aBoolean) {
                            AdminUtil.toast(getActivity(), "Message Sent");
                            AdminUtil.succeed(getActivity());
                        } else {
                            AdminUtil.toast(getActivity(), "Failed to Send Message");
                        }
                    }
                }.execute(data);


            }
        });


        return rootView;
    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        toSelect = cursor.getString(2);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), DataContract.Topic.CONTENT_URI, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

