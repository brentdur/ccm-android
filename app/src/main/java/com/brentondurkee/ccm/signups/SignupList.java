/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
 */

package com.brentondurkee.ccm.signups;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.brentondurkee.ccm.Log;
import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.Utils;
import com.brentondurkee.ccm.provider.DataContract;

/**
     * A placeholder fragment containing a simple view.
     */
    public class SignupList extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

        SimpleCursorAdapter mAdapter;
        final String [] from = new String[]{DataContract.Signup.COLUMN_NAME_NAME, DataContract.Signup.COLUMN_NAME_LOCATION, DataContract.Signup.COLUMN_NAME_MEMBER_COUNT};
        final int [] to = new int[]{R.id.signupName, R.id.signupLocation, R.id.signupCount};
        final String[] PROJECTION = new String[]{
                DataContract.Signup._ID,
                DataContract.Signup.COLUMN_NAME_NAME,
                DataContract.Signup.COLUMN_NAME_LOCATION,
                DataContract.Signup.COLUMN_NAME_MEMBER_COUNT,
                DataContract.Signup.COLUMN_NAME_MEMBER_OF
        };
        public SignupList() {
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.signup, null, from, to, 0);
            mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//                    if (columnIndex == 3){
//                        String date = Utils.dateForm(cursor.getString(columnIndex));
//                        TextView textView = (TextView) view;
//                        textView.setText(date);
//                        return true;
//                    }
                    return false;
                }
            });

            setListAdapter(mAdapter);
            getLoaderManager().initLoader(0, null, this);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), SignupDetail.class);
            intent.putExtra("id", mAdapter.getCursor().getString(0));
            startActivity(intent);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), DataContract.Signup.CONTENT_URI, PROJECTION, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.i("Signup List", ""+data.getCount());
            mAdapter.changeCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }