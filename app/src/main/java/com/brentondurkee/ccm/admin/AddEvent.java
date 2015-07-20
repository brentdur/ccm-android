/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee.
 */

package com.brentondurkee.ccm.admin;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.brentondurkee.ccm.Pager;
import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.provider.DataContract;
import com.brentondurkee.ccm.provider.SyncPosts;
import com.brentondurkee.ccm.provider.SyncUtil;

/**
    This class is the fragment activity and the fragment for the add event activity
 */
public class AddEvent extends FragmentActivity {
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //set toolbar information
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.primaryCCM));
        toolbar.setTitleTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
        setActionBar(toolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new EventAddFragment())
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_event, menu);
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

    public static class EventAddFragment extends Fragment implements AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

        private static final String TAG = "EventAddFragment";

        SimpleCursorAdapter mAdapter;
        String toSelect = "";
        boolean selected = false;
        private View rootView;

        private String[] from = {DataContract.Location.COLUMN_NAME_NAME, DataContract.Location.COLUMN_NAME_NAME};
        private int[] to = {R.id.spinnerTarget, android.R.id.text1};
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
                    data.putString(SyncPosts.EVENT_ADDRESS, ((EditText) rootView.findViewById(R.id.addEventAddress)).getText().toString());
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

                    //run the network io on a different thread
                    new AsyncTask<Bundle, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Bundle... data) {
                            return SyncPosts.addEvent(data[0], SyncUtil.getAccount(), getActivity());
                        }

                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            super.onPostExecute(aBoolean);
                            if (aBoolean) {
                                AdminUtil.toast(getActivity(), "Event Added Successfully");
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
            selected = true;
            if (toSelect.equals("Other")) {
                rootView.findViewById(R.id.addEventAddress).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.addEventName).setVisibility(View.VISIBLE);
                selected = false;
            }
        }

        //Loads the cursor using the provided projection
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), DataContract.Location.CONTENT_URI, PROJECTION, null, null, null);
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

}
