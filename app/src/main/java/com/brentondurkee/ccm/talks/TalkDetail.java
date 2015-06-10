package com.brentondurkee.ccm.talks;

import android.content.ContentResolver;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.provider.DataContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TalkDetail extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new TalkDetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_talk_detail, menu);
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

    public static class TalkDetailFragment extends Fragment {

        Cursor mCursor;
        ContentResolver mResolve;
        final String[] PROJECTION = new String[]{
                DataContract.Talk._ID,
                DataContract.Talk.COLUMN_NAME_SUBJECT,
                DataContract.Talk.COLUMN_NAME_AUTHOR,
                DataContract.Talk.COLUMN_NAME_DATE,
                DataContract.Talk.COLUMN_NAME_REFERENCE,
                DataContract.Talk.COLUMN_NAME_OUTLINE
        };

        public TalkDetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle extras = getActivity().getIntent().getExtras();
            String id = extras.getString("id");
            mResolve = getActivity().getContentResolver();
            mCursor = mResolve.query(DataContract.Talk.CONTENT_URI, PROJECTION, DataContract.Talk._ID + "='" + id + "'", null, null);
            mCursor.moveToFirst();
            String subject = mCursor.getString(1);
            String author = mCursor.getString(2);
            String date = mCursor.getString(3);
            String reference = mCursor.getString(4);
            String outline = mCursor.getString(5);
            outline = "->> " + outline;
            outline = outline.replace("\",,,\"", "\n ->> ");
            Date time;
            View rootView = inflater.inflate(R.layout.fragment_talk_detail, container, false);
            try {
                date = date.replace("Z", " GMT");
                time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS zzz").parse(date);
                long millis = (time.getTime() - System.currentTimeMillis());
                long seconds = (millis/1000)%60;
                long mins = (millis/60000)%60;
                long hours = (millis/3600000)%24;
                long days = (millis/86400000);
                date = String.format("%d days %d:%d:%d", days, hours, mins, seconds);
                Log.v("Time Parse", time.toString());
            }
            catch (ParseException e){
                Log.w("Time Parse Exception", e.toString());
            }
            ((TextView) rootView.findViewById(R.id.talkDetailTopic)).setText(subject);
            ((TextView) rootView.findViewById(R.id.talkDetailAuthor)).setText(author);
            ((TextView) rootView.findViewById(R.id.talkDetailTime)).setText(date);
            ((TextView) rootView.findViewById(R.id.talkDetailVerse)).setText(reference);
            ((TextView) rootView.findViewById(R.id.talkDetailOutline)).setText(outline);
            return rootView;
        }
    }
}
