/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee.
 */

package com.brentondurkee.ccm.admin;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
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
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.brentondurkee.ccm.Log;
import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.provider.DataContract;
import com.brentondurkee.ccm.provider.SyncPosts;
import com.brentondurkee.ccm.provider.SyncUtil;

/**
    This class is the fragment activity and the fragment for the add event activity
    Relies on AdminUtil
 */
public class EventAddFragment extends Fragment implements AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "EventAddFragment";

    SimpleCursorAdapter mAdapter;
    String toSelect = "";
    boolean selected = false;
    private View rootView;

    private final String[] from = {DataContract.Location.COLUMN_NAME_NAME, DataContract.Location.COLUMN_NAME_NAME};
    private final int[] to = {R.id.spinnerTarget, android.R.id.text1};
    private static final String[] PROJECTION = new String[]{
            DataContract.Location._ID,
            DataContract.Location.COLUMN_NAME_NAME
    };

    public EventAddFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //get the cursor adapter to load the pre-fab locations
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.spinner_layout, null, from, to, 0);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getLoaderManager().initLoader(0, null, this);

        //inflate the view, add the spinner and button
        final View rootView = inflater.inflate(R.layout.fragment_add_event, container, false);
        this.rootView = rootView;

        Spinner spin = (Spinner) rootView.findViewById(R.id.spinner_place);
        spin.setAdapter(mAdapter);
        spin.setOnItemSelectedListener(this);

        //when submit is pressed
        rootView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle data = new Bundle();
                data.putString(SyncPosts.EVENT_TITLE, ((EditText) rootView.findViewById(R.id.addEventTitle)).getText().toString());
                data.putString(SyncPosts.EVENT_DATE, ((EditText) rootView.findViewById(R.id.addEventDate)).getText().toString());
                data.putString(SyncPosts.EVENT_ADDRESS, "");
                String text;
                //if a pre-fab location has been selected, then use that, else load the name
                //  and address
                if (selected) {
                    text = toSelect;
                } else {
                    text = ((EditText) rootView.findViewById(R.id.addEventName)).getText().toString();
                    data.putString(SyncPosts.EVENT_ADDRESS, ((EditText) rootView.findViewById(R.id.addEventAddress)).getText().toString());
                }
                data.putString(SyncPosts.EVENT_LOCATION, text);
                data.putString(SyncPosts.EVENT_DESCRIPTION, ((EditText) rootView.findViewById(R.id.addEventDesc)).getText().toString());

                AdminUtil.showDialog(getActivity());
                //run the network io on a different thread
                new AsyncTask<Bundle, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Bundle... data) {
                        return SyncPosts.addEvent(data[0], SyncUtil.getAccount(), getActivity());
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        super.onPostExecute(aBoolean);
                        AdminUtil.hideDialog();
                        if (aBoolean) {
                            AdminUtil.toast(getActivity(), "Event Added Successfully");
                            SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_EVENT);
                            AdminUtil.succeed(getActivity());
                        } else {
                            AdminUtil.toast(getActivity(), "Failed to Add Event");
                        }

                    }
                }.execute(data);
            }
        });

        return rootView;
    }

    /**
     Responds to item selects on the spinner
     If the item is anything besides Other then it just uses that
     If the item is "Other" then it reveals two new fields.
     * @param parent the Adapter View
     * @param view the view affected
     * @param position the position in the spinner
     * @param id the spinner id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        Log.v(TAG, cursor.getString(1));
        toSelect = cursor.getString(1);

        if (toSelect.equals("Other")) {
            rootView.findViewById(R.id.addEventAddress).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.addEventName).setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            p.addRule(RelativeLayout.BELOW, R.id.addEventAddress);
            rootView.findViewById(R.id.addEventDesc).setLayoutParams(p);
            selected = false;
        }
        else {
            if(!selected){
                RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                p.addRule(RelativeLayout.BELOW, R.id.spinner_place);
                rootView.findViewById(R.id.addEventAddress).setVisibility(View.INVISIBLE);
                rootView.findViewById(R.id.addEventName).setVisibility(View.INVISIBLE);
                rootView.findViewById(R.id.addEventDesc).setLayoutParams(p);
            }
            selected = true;

        }
    }

    //Loads the cursor using the provided projection
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), DataContract.Location.CONTENT_URI, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        MatrixCursor extras = new MatrixCursor(new String[] {DataContract.Location._ID, DataContract.Location.COLUMN_NAME_NAME});
        extras.addRow(new String[]{"-1", "Other"});
        Cursor extendedCursor = new MergeCursor(new Cursor[]{data, extras});

        mAdapter.changeCursor(extendedCursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}