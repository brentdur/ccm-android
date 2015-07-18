package com.brentondurkee.ccm.admin;

import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toolbar;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.provider.DataContract;
import com.brentondurkee.ccm.provider.SyncPosts;
import com.brentondurkee.ccm.provider.SyncUtil;

public class AddMsg extends FragmentActivity {
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.primaryCCM));
        toolbar.setTitleTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
        setActionBar(toolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MsgAddFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_msg, menu);
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

    public static class MsgAddFragment extends Fragment implements AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

        private String[] from = {DataContract.Group.COLUMN_NAME_NAME, DataContract.Group.COLUMN_NAME_NAME};
        private int[] to = {R.id.spinnerTarget, android.R.id.text1};
        private static final String[] PROJECTION = new String[]{
                DataContract.Group._ID,
                DataContract.Group.COLUMN_NAME_NAME
        };

        private TextView reference;
        private TextView fullRef;
        private boolean open = false;
        private TextView openButton;

        private final String TAG = getClass().getSimpleName();

        SimpleCursorAdapter mAdapter;
        Spinner spin;
        String toSelect = "";

        public MsgAddFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

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
                    data.putString(SyncPosts.MSG_FROM, ((EditText) rootView.findViewById(R.id.msgAddFrom)).getText().toString());
                    data.putString(SyncPosts.MSG_SUBJECT, ((EditText) rootView.findViewById(R.id.msgAddSubject)).getText().toString());
                    data.putString(SyncPosts.MSG_MESSAGE, ((EditText) rootView.findViewById(R.id.msgAddMsg)).getText().toString());
                    if(toSelect.equals("")){
                        //TODO: Add empty Toast
                    }
                    data.putString(SyncPosts.MSG_TO, toSelect);

                    new AsyncTask<Bundle, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Bundle... data) {
                            return SyncPosts.addMsg(data[0], SyncUtil.getAccount(), getActivity());
                        }

                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            super.onPostExecute(aBoolean);
                            //TODO: display toast to user
                        }
                    }.execute(data);


                }
            });


            return rootView;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.v(TAG, parent.getItemAtPosition(position).toString());
            toSelect = parent.getItemAtPosition(position).toString();
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), DataContract.Group.CONTENT_URI, PROJECTION, null, null, null);
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
