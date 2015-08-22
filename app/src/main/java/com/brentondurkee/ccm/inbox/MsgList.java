package com.brentondurkee.ccm.inbox;

import android.database.CursorWrapper;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toolbar;

import com.brentondurkee.ccm.Log;
import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.Utils;
import com.brentondurkee.ccm.admin.AdminUtil;
import com.brentondurkee.ccm.provider.DataContract;
import com.brentondurkee.ccm.provider.SyncPosts;
import com.brentondurkee.ccm.provider.SyncUtil;

/**
 * Created by brenton on 6/12/15.
 *
 * List fragment for messages
 */
public class MsgList extends FragmentActivity {

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Log.v("Msg List", "AA");

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.primaryCCM));
        toolbar.setTitleTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
        setActionBar(toolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MsgListFragment())
                    .commit();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_inbox, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings){
            SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_MSG);
        }
        return super.onOptionsItemSelected(item);
    }


    public static class MsgListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

        MessageCursorAdapter mAdapter;
        final String[] FROM = new String[]{DataContract.Msg.COLUMN_NAME_SIMPLE_FROM, DataContract.Msg.COLUMN_NAME_SUBJECT, DataContract.Msg.COLUMN_NAME_DATE};
        final int[] TO = new int[]{R.id.msgFrom, R.id.msgSubject, R.id.msgTime};
        final String[] PROJECTION = new String[]{
                DataContract.Msg._ID,
                DataContract.Msg.COLUMN_NAME_SIMPLE_FROM,
                DataContract.Msg.COLUMN_NAME_SUBJECT,
                DataContract.Msg.COLUMN_NAME_DATE,
                DataContract.Msg.COLUMN_NAME_ENTRY_ID,
                DataContract.Msg.COLUMN_NAME_TOPIC
        };

        final String[] TOPIC_PROJECTION = new String[]{
                DataContract.Topic._ID,
                DataContract.Topic.COLUMN_NAME_ENTRY_ID,
                DataContract.Topic.COLUMN_NAME_NAME
        };

        public MsgListFragment() {
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mAdapter = new MessageCursorAdapter(getActivity(), null, 0);
            getLoaderManager().initLoader(1, null, this);
            setEmptyText("No messages");
            registerForContextMenu(getListView());
            setListAdapter(mAdapter);
            getLoaderManager().initLoader(0, null, this);
        }



        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), MsgDetail.class);
            intent.putExtra("id", mAdapter.getCursor().getString(0));
            intent.putExtra("entry_id", mAdapter.getCursor().getString(4));
            startActivity(intent);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater menuInflater = getActivity().getMenuInflater();
            menuInflater.inflate(R.menu.msg_detail_menu, menu);
        }

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            if(item.getItemId() == R.id.delete_msg){
                Bundle data = new Bundle();
                data.putString(SyncPosts.DELETE_MESSAGE, ((CursorWrapper) mAdapter.getItem(info.position)).getString(4));
                new AsyncTask<Bundle, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Bundle... data) {
                        return SyncPosts.deleteMsg(data[0], SyncUtil.getAccount(), getActivity());
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        super.onPostExecute(aBoolean);
                        if (aBoolean) {
                            SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_MSG);
                        } else {
                            AdminUtil.toast(getActivity(), "Failed to Delete");
                        }

                    }
                }.execute(data);
            }
            return super.onContextItemSelected(item);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v("Msg List", "Loaded");
            if(id == 1) {
                return new CursorLoader(getActivity(), DataContract.Topic.CONTENT_URI, TOPIC_PROJECTION, null, null, null);
            }
            else {
                return new CursorLoader(getActivity(), DataContract.Msg.CONTENT_URI, PROJECTION, null, null, DataContract.Msg.COLUMN_NAME_TOPIC);
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.v("Msg List", "Changed");
            if (loader.getId() == 1){
                mAdapter.setTopicCursor(data);
            }
            else {
                mAdapter.changeCursor(data);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
