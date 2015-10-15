/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.provider.DataContract;
import com.brentondurkee.ccm.provider.SyncPosts;
import com.brentondurkee.ccm.provider.SyncUtil;

import java.util.ArrayList;


/**
 * Add message activity for posting new messages
 */
public class BcAddFragment extends Fragment implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TOPIC_ADAPTER_ID = 0;
    private static final int GROUP_ADAPTER_ID = 1;

    private final String[] from = {DataContract.Topic.COLUMN_NAME_NAME, DataContract.Topic.COLUMN_NAME_NAME};
    private final int[] to = {R.id.spinnerTarget, android.R.id.text1};
    private static final String[] TOPIC_PROJECTION = new String[]{
            DataContract.Topic._ID,
            DataContract.Topic.COLUMN_NAME_NAME,
            DataContract.Topic.COLUMN_NAME_ENTRY_ID
    };

    private static final String[] GROUP_PROJECTION = new String[]{
            DataContract.Group._ID,
            DataContract.Group.COLUMN_NAME_NAME,
            DataContract.Group.COLUMN_NAME_ENTRY_ID
    };

    SimpleCursorAdapter topicAdapter;
    SimpleCursorAdapter groupAdapter;
    Spinner topicSpinner;
    ListView syncsList;
    ListView groupList;
    ArrayList<String> topicSelect = new ArrayList<String>(5);
    ArrayList<String> groupSelect = new ArrayList<String>(5);


    public BcAddFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //loads receiving groups from cursor
//        topicAdapter = new SimpleCursorAdapter(getActivity(), R.layout.spinner_layout, null, from, to, 0);
        topicAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_layout, null, from, to, 0);
//        topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        topicAdapter.setDropDownViewResource(android.R.layout.simple_list_item_multiple_choice);
        getLoaderManager().initLoader(TOPIC_ADAPTER_ID, null, this);

//        groupAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_layout, null,
        groupAdapter.setViewResource(android.R.layout.simple_list_item_multiple_choice);
        getLoaderManager().initLoader(GROUP_ADAPTER_ID, null, this);

        final View rootView = inflater.inflate(R.layout.fragment_add_bc, container, false);
//        topicSpinner = (Spinner) rootView.findViewById(R.id.spinner_to);
//        topicSpinner.setAdapter(topicAdapter);
//        topicSpinner.setOnItemSelectedListener(this);
        Switch syncCastButton = (Switch) rootView.findViewById(R.id.synyCastSwitch);
        //TODO add logic to hide fields when it's a cast
//        syncCastButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        syncsList = (ListView) rootView.findViewById(R.id.list_syncs);
        syncsList.setAdapter(topicAdapter);
        syncsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        syncsList.setItemsCanFocus(false);
        syncsList.setOnItemClickListener(this);

        groupList = (ListView) rootView.findViewById(R.id.list_groups);
        groupList.setAdapter(groupAdapter);
        groupList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        groupList.setItemsCanFocus(false);
        groupList.setOnItemClickListener(this);

        rootView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle data = new Bundle();
                data.putString(SyncPosts.BROADCAST_TITLE, ((EditText) rootView.findViewById(R.id.bcAddTitle)).getText().toString());
                data.putString(SyncPosts.BROADCAST_MSG, ((EditText) rootView.findViewById(R.id.bcAddMsg)).getText().toString());
                final boolean isCast = ((Switch) rootView.findViewById(R.id.synyCastSwitch)).isChecked();

                if (topicSelect.isEmpty()) {
                    topicSelect.add("all");
                }

                data.putStringArray(SyncPosts.BROADCAST_SYNCS, (String [])topicSelect.toArray());
                data.putStringArray(SyncPosts.BROADCAST_RECP, (String []) groupSelect.toArray());

                AdminUtil.showDialog(getActivity());

                new AsyncTask<Bundle, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Bundle... data) {
                        return SyncPosts.addBroadcast(data[0], SyncUtil.getAccount(), getActivity(), isCast);
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        super.onPostExecute(aBoolean);
                        AdminUtil.hideDialog();
                        if (aBoolean) {
                            AdminUtil.toast(getActivity(), "Broadcast Sent");
                            AdminUtil.succeed(getActivity());
                        } else {
                            AdminUtil.toast(getActivity(), "Failed to Send Broadcast");
                        }
                    }
                }.execute(data);


            }
        });


        return rootView;
    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getAdapter().equals(topicAdapter)){
            Cursor cursor = (Cursor) topicAdapter.getItem(position);
            topicSelect.add(cursor.getString(2));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getAdapter().equals(topicAdapter)){
            Cursor cursor = (Cursor) topicAdapter.getItem(position);
            if (topicSelect.contains(cursor.getString(2))) {
                topicSelect.remove(cursor.getString(2));
            }
            else {
                topicSelect.add(cursor.getString(2));
            }
        }
        else if (parent.getAdapter().equals(groupAdapter)) {
            Cursor cursor = (Cursor) groupAdapter.getItem(position);
            if (groupSelect.contains(cursor.getString(2))) {
                groupSelect.remove(cursor.getString(2));
            }
            else {
                groupSelect.add(cursor.getString(2));
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case TOPIC_ADAPTER_ID:
                return new CursorLoader(getActivity(), DataContract.Topic.CONTENT_URI, TOPIC_PROJECTION, null, null, null);
            case GROUP_ADAPTER_ID:
                return new CursorLoader(getActivity(), DataContract.Group.CONTENT_URI, GROUP_PROJECTION, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case TOPIC_ADAPTER_ID:
                topicAdapter.changeCursor(data);
                break;
            case GROUP_ADAPTER_ID:
                groupAdapter.changeCursor(data);
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch(loader.getId()) {
            case TOPIC_ADAPTER_ID:
                topicAdapter.changeCursor(null);
                break;
            case GROUP_ADAPTER_ID:
                groupAdapter.changeCursor(null);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

