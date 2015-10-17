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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;

import com.brentondurkee.ccm.Log;
import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.provider.DataContract;
import com.brentondurkee.ccm.provider.SyncPosts;
import com.brentondurkee.ccm.provider.SyncUtil;

import java.util.ArrayList;


/**
 * Add message activity for posting new messages
 */
public class BcAddFragment extends Fragment implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TOPIC_ADAPTER_ID = 0;
    private static final int GROUP_ADAPTER_ID = 1;

    private final String[] fromG = {DataContract.Group.COLUMN_NAME_NAME, DataContract.Group.COLUMN_NAME_NAME};
    private final int[] to = {R.id.listTarget, android.R.id.text1};

    private static final String[] GROUP_PROJECTION = new String[]{
            DataContract.Group._ID,
            DataContract.Group.COLUMN_NAME_NAME,
            DataContract.Group.COLUMN_NAME_ENTRY_ID
    };

    ArrayAdapter<String> syncsAdapter;
    SimpleCursorAdapter groupAdapter;
    ListView syncsList;
    ListView groupList;
    ArrayList<String> syncSelect = new ArrayList<String>(5);
    ArrayList<String> groupSelect = new ArrayList<String>(5);

    final String[] syncOptions = {"events", "signups", "talks", "groups", "locations", "topics", "broadcasts", "conversations"};


    public BcAddFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        syncsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, syncOptions);

        groupAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_layout, null, fromG, to, 0);
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
        syncsList.setAdapter(syncsAdapter);
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

                if (syncSelect.isEmpty()) {
                    syncSelect.add("all");
                }
                for (int i = 0; i < syncSelect.size(); i++){
                    Log.v("List", syncSelect.get(i));
                }
                String[] topicArray = syncSelect.toArray(new String[syncSelect.size()]);
                String[] groupArray = groupSelect.toArray(new String[groupSelect.size()]);
                data.putStringArray(SyncPosts.BROADCAST_SYNCS, topicArray);
                data.putStringArray(SyncPosts.BROADCAST_RECP, groupArray);

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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getAdapter().equals(syncsAdapter)){
            String current = syncsAdapter.getItem(position);
            if (syncSelect.contains(current)) {
                syncSelect.remove(current);
            }
            else {
                syncSelect.add(current);
            }
        }
        else if (parent.getAdapter().equals(groupAdapter)) {
            Cursor cursor = (Cursor) groupAdapter.getItem(position);
            if (groupSelect.contains(cursor.getString(1))) {
                groupSelect.remove(cursor.getString(1));
            }
            else {
                groupSelect.add(cursor.getString(1));
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case GROUP_ADAPTER_ID:
                return new CursorLoader(getActivity(), DataContract.Group.CONTENT_URI, GROUP_PROJECTION, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case GROUP_ADAPTER_ID:
                groupAdapter.changeCursor(data);
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch(loader.getId()) {
            case GROUP_ADAPTER_ID:
                groupAdapter.changeCursor(null);
                break;
        }
    }
}

