package com.brentondurkee.ccm.events;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.CursorLoader;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.provider.DataContract;


public class Events extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        FragmentManager fragManager = getFragmentManager();
        FragmentTransaction fragTrans = fragManager.beginTransaction();
//        fragTrans.add(R.id.container, new EventList());
        fragTrans.commit();
    }


    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_events, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class EventList extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private SimpleCursorAdapter mAdapter;
        private String[] from = {DataContract.Event.COLUMN_NAME_TITLE, DataContract.Event.COLUMN_NAME_DATE};
        private int[] to = {R.id.eventTitle, R.id.eventDate};
        private static final String[] PROJECTION = new String[]{
                DataContract.Event._ID,
                DataContract.Event.COLUMN_NAME_TITLE,
                DataContract.Event.COLUMN_NAME_DATE,
                DataContract.Event.COLUMN_NAME_LOCATION
        };
        private final String TAG=getClass().getSimpleName();

        public Event[] eventsArray = Event.createList(10);

        public EventList() {
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.event, null, from, to, 0);
            Log.v(TAG, "list created");

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
}

class EventAdapter extends ArrayAdapter<Event> {
    LayoutInflater layoutInflater;

    EventAdapter(Context context, int resource, int textViewResourceId, Event[] objects) {
        super(context, resource, textViewResourceId, objects);

        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Event event = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.event, parent, false);
        }
        TextView title = (TextView) convertView.findViewById(R.id.eventTitle);
        TextView location = (TextView) convertView.findViewById(R.id.eventLocation);
        TextView date = (TextView) convertView.findViewById(R.id.eventDate);

        title.setText(event.title);
        location.setText(event.location);
        date.setText(event.date);

        return convertView;
    }
}


