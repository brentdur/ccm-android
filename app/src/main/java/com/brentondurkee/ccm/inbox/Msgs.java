package com.brentondurkee.ccm.inbox;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.content.Context;
import android.support.v4.content.CursorLoader;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.content.Intent;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.provider.DataContract;


public class Msgs extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msgs);

        FragmentManager fragManager = getFragmentManager();
        FragmentTransaction fragTrans = fragManager.beginTransaction();
//        fragTrans.add(R.id.container, new MsgList());
        fragTrans.commit();
        findViewById(R.id.container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Msgs.this, MsgDetail.class));
            }
        });
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
    public static class MsgList extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

//        public Message[] msgsArray = Message.createList(10);
        SimpleCursorAdapter mAdapter;
        final String[] FROM = new String[]{DataContract.Msg.COLUMN_NAME_FROM, DataContract.Msg.COLUMN_NAME_SUBJECT};
        final int[] TO = new int[]{R.id.msgFrom, R.id.msgSubject};
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
//            MsgsAdapter adapter = new MsgsAdapter(getActivity(), R.layout.message, R.id.msgSubject, msgsArray);
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
}

class MsgsAdapter extends ArrayAdapter<Message> {
    LayoutInflater layoutInflater;

    MsgsAdapter(Context context, int resource, int textViewResourceId, Message[] objects) {
        super(context, resource, textViewResourceId, objects);

        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.message, parent, false);
        }
        TextView subject = (TextView) convertView.findViewById(R.id.msgSubject);
        TextView from = (TextView) convertView.findViewById(R.id.msgFrom);
        TextView time = (TextView) convertView.findViewById(R.id.msgTime);

        subject.setText(message.subject);
        from.setText(message.from);
        time.setText(message.time);

        return convertView;
    }
}


