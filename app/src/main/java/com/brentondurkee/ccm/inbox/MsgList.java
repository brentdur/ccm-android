package com.brentondurkee.ccm.inbox;

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
 * Created by brenton on 6/12/15.
 */
public class MsgList extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //        public Message[] msgsArray = Message.createList(10);
    SimpleCursorAdapter mAdapter;
    final String[] FROM = new String[]{DataContract.Msg.COLUMN_NAME_FROM, DataContract.Msg.COLUMN_NAME_SUBJECT, DataContract.Msg.COLUMN_NAME_DATE};
    final int[] TO = new int[]{R.id.msgFrom, R.id.msgSubject, R.id.msgTime};
    final String[] PROJECTION = new String[]{
            DataContract.Msg._ID,
            DataContract.Msg.COLUMN_NAME_FROM,
            DataContract.Msg.COLUMN_NAME_SUBJECT,
            DataContract.Msg.COLUMN_NAME_DATE
    };

    public MsgList() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.message, null, FROM, TO, 0);

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
        intent.setClass(getActivity(), MsgDetail.class);
        intent.putExtra("id", mAdapter.getCursor().getString(0));
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), DataContract.Msg.CONTENT_URI, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
