package com.brentondurkee.ccm.events;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.provider.DataContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class EventDetail extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new EventDetailFragment())
                    .commit();
        }



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_detail, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class EventDetailFragment extends Fragment {

        Cursor cursor;
        ContentResolver mResolver;
        Thread t;

        final String[] PROJECTION = new String[]{
                DataContract.Event.COLUMN_NAME_TITLE,
                DataContract.Event.COLUMN_NAME_LOCATION,
                DataContract.Event.COLUMN_NAME_DATE,
                DataContract.Event.COLUMN_NAME_DESCRIPTION
        };

        final String selection = DataContract.Event._ID + " = '";

        public EventDetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle extras = getActivity().getIntent().getExtras();
            String id = extras.getString("id");
            mResolver = getActivity().getContentResolver();
            cursor = mResolver.query(DataContract.Event.CONTENT_URI, PROJECTION, selection + id + "'", null, null);
            Log.v("Event Detail", ""+cursor.getCount());
            cursor.moveToFirst();
            String title = cursor.getString(0);
            String location = cursor.getString(1);
            String date = cursor.getString(2);
            Date time;
            View rootView = inflater.inflate(R.layout.fragment_event_detail, container, false);
            try {
                date = date.replace("Z", " GMT");
                time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS zzz").parse(date);
                long millis = (time.getTime() - System.currentTimeMillis());
                long seconds = (millis/1000)%60;
                long mins = (millis/60000)%60;
                long hours = (millis/3600000)%24;
                long days = (millis/86400000);
                timer(days, hours, mins, seconds, rootView);
                date = String.format("%d days %d:%d:%d", days, hours, mins, seconds);
                Log.v("Time Parse", time.toString());
            }
            catch (ParseException e){
                Log.w("Time Parse Exception", e.toString());
            }
            String description = cursor.getString(3);
            cursor.close();

            ((TextView) rootView.findViewById(R.id.eventDetailTitle)).setText(title);
            ((TextView) rootView.findViewById(R.id.eventDetailLocation)).setText(location);
            ((TextView) rootView.findViewById(R.id.eventDetailDate)).setText(date);
            ((TextView) rootView.findViewById(R.id.eventDetailDesc)).setText(description);



            return rootView;
        }


        public void timer(final long days, final long hours, final long minutes, final long seconds, final View rootView){
            t = new Thread() {
                long secs = seconds;
                long mins = minutes;
                long hrs = hours;
                long dys = days;
                @Override
                public void run() {
                    try {
                        while (!isInterrupted()) {
                            Thread.sleep(1000);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    secs--;
                                    if (secs < 0) {
                                        secs = 59;
                                        mins--;
                                        if (mins < 0) {
                                            mins = 59;
                                            hrs--;
                                            if (hrs < 0) {
                                                hrs = 23;
                                                dys--;
                                            }
                                        }
                                    }

                                    String date;
                                    if (dys > 0) {
                                        date = String.format("%d days %d:%d:%d", dys, hrs, mins, secs);
                                    } else {
                                        date = String.format("%d:%d:%d", hrs, mins, secs);
                                    }
                                    ((TextView) rootView.findViewById(R.id.eventDetailDate)).setText(date);
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                    }
                }
            };

            t.start();
        }

        @Override
        public void onPause() {
            super.onPause();
            t.interrupt();
        }
    }
}
