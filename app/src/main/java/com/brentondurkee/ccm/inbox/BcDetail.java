/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
 */

package com.brentondurkee.ccm.inbox;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.Utils;
import com.brentondurkee.ccm.admin.AdminUtil;
import com.brentondurkee.ccm.provider.DataContract;
import com.brentondurkee.ccm.provider.SyncPosts;
import com.brentondurkee.ccm.provider.SyncUtil;


/**
 * Created by brenton on 6/12/15.
 *
 * Detail fragment for Messages
 */
public class BcDetail extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.primaryCCM));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new BCDetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mInflate = getMenuInflater();
        mInflate.inflate(R.menu.msg_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.delete_msg){
            String bcId = getIntent().getExtras().getString("id");
            Bundle data = new Bundle();
            final Context context = this;
            data.putString(SyncPosts.BROADCAST_ID, bcId);
            new AsyncTask<Bundle, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Bundle... data) {
                    return SyncPosts.putKillBroadcast(data[0], SyncUtil.getAccount(), context);
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    super.onPostExecute(aBoolean);
                    if (aBoolean) {
                        SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_BC);
                        finish();
                    } else {
                        AdminUtil.toast(getApplicationContext(), "Failed to Delete");
                    }

                }
            }.execute(data);
        }
        if (id == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class BCDetailFragment extends Fragment {

        final String[] PROJECTION = new String[]{
                DataContract.Broadcast.COLUMN_NAME_TITLE,
                DataContract.Broadcast.COLUMN_NAME_MSG,
                DataContract.Broadcast.COLUMN_NAME_DATE
        };

        public BCDetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle extras = getActivity().getIntent().getExtras();
            String id = extras.getString("id");
            Cursor mCursor = getActivity().getContentResolver().query(DataContract.Broadcast.CONTENT_URI, PROJECTION, DataContract.Broadcast._ID + "='" + id + "'", null, null);
            mCursor.moveToFirst();
            String title = mCursor.getString(0);
            String message = mCursor.getString(1);
            String date = Utils.dateForm(mCursor.getString(2));

            View rootView = inflater.inflate(R.layout.fragment_bc_detail, container, false);
            ((TextView) rootView.findViewById(R.id.bcDetailTitle)).setText(title);
            ((TextView) rootView.findViewById(R.id.bcDetailTime)).setText(date);
            ((TextView) rootView.findViewById(R.id.bcDetailMsg)).setText(message);
            mCursor.close();

            return rootView;
        }
    }
}
