package com.brentondurkee.ccm.talks;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.support.v4.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.CursorLoader;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.provider.DataContract;

public class Talks extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talks);

        FragmentManager fragManager = getFragmentManager();
        FragmentTransaction fragTrans = fragManager.beginTransaction();
//        fragTrans.add(R.id.container, new TalkList());
        fragTrans.commit();
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_talks, menu);
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
    public static class TalkList extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

//        public Talk[] talkArray = Talk.createList(10);
        SimpleCursorAdapter mAdapter;
        final String [] from = new String[]{DataContract.Talk.COLUMN_NAME_SUBJECT, DataContract.Talk.COLUMN_NAME_AUTHOR, DataContract.Talk.COLUMN_NAME_DATE};
        final int [] to = new int[]{R.id.talkTopic, R.id.talkAuthor, R.id.talkTime};
        final String[] PROJECTION = new String[]{
                DataContract.Talk._ID,
                DataContract.Talk.COLUMN_NAME_SUBJECT,
                DataContract.Talk.COLUMN_NAME_AUTHOR,
                DataContract.Talk.COLUMN_NAME_DATE
        };
        public TalkList() {
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.talk, null, from, to, 0);
//            TalksAdapter adapter = new TalksAdapter(getActivity (), R.layout.talk, R.id.talkTopic, talkArray);
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
}

class TalksAdapter extends ArrayAdapter<Talk> {
    LayoutInflater layoutInflater;

    TalksAdapter(Context context, int resource, int textViewResourceId, Talk[] objects) {
        super(context, resource, textViewResourceId, objects);

        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Talk talk = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.talk, parent, false);
        }
        TextView topic = (TextView) convertView.findViewById(R.id.talkTopic);
        TextView author = (TextView) convertView.findViewById(R.id.talkAuthor);
        TextView time = (TextView) convertView.findViewById(R.id.talkTime);
        TextView verse = (TextView) convertView.findViewById(R.id.talkVerse);

        topic.setText(talk.topic);
        author.setText(talk.author);
        time.setText(talk.time);
        verse.setText(talk.verse);

        return convertView;
    }
}
