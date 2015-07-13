package com.brentondurkee.ccm.events;


import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.Utils;
import com.brentondurkee.ccm.provider.DataContract;


/**
 * Created by brenton on 6/12/15.
 */
/**
 * A placeholder fragment containing a simple view.
 */
public class EventList extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter mAdapter;
    private String[] from = {DataContract.Event.COLUMN_NAME_TITLE, DataContract.Event.COLUMN_NAME_DATE, DataContract.Event.COLUMN_NAME_LOCATION};
    private int[] to = {R.id.eventTitle, R.id.eventDate, R.id.eventLocation};
    private static final String[] PROJECTION = new String[]{
            DataContract.Event._ID,
            DataContract.Event.COLUMN_NAME_TITLE,
            DataContract.Event.COLUMN_NAME_DATE,
            DataContract.Event.COLUMN_NAME_LOCATION
    };
    private final String TAG=getClass().getSimpleName();

    public EventList() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.event, null, from, to, 0);
        Log.v(TAG, "list created");
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == 2){
                    String date = Utils.dateForm(cursor.getString(columnIndex));
                    TextView textView = (TextView) view;
                    textView.setText(date);
                    return true;
                }
                return false;
            }
        });

//            EventAdapter adapter = new EventAdapter(getActivity(), R.layout.event, R.id.eventTitle, eventsArray);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), EventDetail.class);
        intent.putExtra("id", mAdapter.getCursor().getString(0));
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), DataContract.Event.CONTENT_URI, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

}
