package com.brentondurkee.ccm.talks;

import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.Utils;
import com.brentondurkee.ccm.provider.DataContract;

/**
     * A placeholder fragment containing a simple view.
     */
    public class TalkList extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

//        public Talk[] talkArray = Talk.createList(10);
        SimpleCursorAdapter mAdapter;
        final String [] from = new String[]{DataContract.Talk.COLUMN_NAME_SUBJECT, DataContract.Talk.COLUMN_NAME_AUTHOR, DataContract.Talk.COLUMN_NAME_DATE, DataContract.Talk.COLUMN_NAME_REFERENCE};
        final int [] to = new int[]{R.id.talkTopic, R.id.talkAuthor, R.id.talkTime, R.id.talkVerse};
        final String[] PROJECTION = new String[]{
                DataContract.Talk._ID,
                DataContract.Talk.COLUMN_NAME_SUBJECT,
                DataContract.Talk.COLUMN_NAME_AUTHOR,
                DataContract.Talk.COLUMN_NAME_DATE,
                DataContract.Talk.COLUMN_NAME_REFERENCE
        };
        public TalkList() {
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.talk, null, from, to, 0);

            mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if (columnIndex == 3){
                        String date = Utils.dateForm(cursor.getString(columnIndex));
                        TextView textView = (TextView) view;
                        textView.setText(date);
                        return true;
                    }
                    return false;
                }
            });

            setListAdapter(mAdapter);
            getLoaderManager().initLoader(0, null, this);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), TalkDetail.class);
            intent.putExtra("id", mAdapter.getCursor().getString(0));
            startActivity(intent);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), DataContract.Talk.CONTENT_URI, PROJECTION, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.changeCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }