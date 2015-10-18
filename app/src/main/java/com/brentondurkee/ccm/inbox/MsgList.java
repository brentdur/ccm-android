package com.brentondurkee.ccm.inbox;

import android.content.Intent;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.brentondurkee.ccm.Log;
import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.admin.AdminActivity;
import com.brentondurkee.ccm.admin.AdminUtil;
import com.brentondurkee.ccm.provider.DataContract;
import com.brentondurkee.ccm.provider.SyncPosts;
import com.brentondurkee.ccm.provider.SyncUtil;
import com.commonsware.cwac.merge.MergeAdapter;

/**
 * Created by brenton on 6/12/15.
 *
 * List fragment for messages
 */
public class MsgList extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar;

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.primaryCCM));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(!SyncUtil.isMinister){
            if(menu.findItem(R.id.add_msg) == null){
                menu.add(Menu.NONE, R.id.add_msg, Menu.NONE, R.string.add_msg);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent openA;
        if (item.getItemId() == R.id.action_settings){
            SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_CONVO);
            SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_BC);
            return true;
        }
        else if (item.getItemId() == R.id.add_msg){
            openA = new Intent(getBaseContext(), AdminActivity.class);
            openA.putExtra(AdminUtil.ADD_TYPE, AdminUtil.TYPE_MSG);
        }
        else {
            return super.onOptionsItemSelected(item);
        }
        startActivityForResult(openA, 0);

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.v("Activity Result", "Activity Resulted");
        MsgListFragment listFrag = (MsgListFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        listFrag.adapterNotifies();
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static class MsgListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

        CommunicationsCursorAdapter bcAdapter;
        CommunicationsCursorAdapter convoAdapter;
        MergeAdapter mergeAdapter = new MergeAdapter();
        final String[] PROJECTION = new String[]{
                DataContract.Convo._ID,
                DataContract.Convo.COLUMN_NAME_SUBJECT,
                DataContract.Convo.COLUMN_NAME_FROM,
                DataContract.Convo.COLUMN_NAME_TOPIC,
                DataContract.Convo.COLUMN_NAME_ENTRY_ID
        };

        final String[] BC_PROJECTION = new String[]{
                DataContract.Broadcast._ID,
                DataContract.Broadcast.COLUMN_NAME_TITLE,
                DataContract.Broadcast.COLUMN_NAME_ENTRY_ID
        };

        public MsgListFragment() {
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            bcAdapter = new CommunicationsCursorAdapter(getActivity(), null, 0);
            convoAdapter = new CommunicationsCursorAdapter(getActivity(), null, 0);
            mergeAdapter.addAdapter(bcAdapter);
            mergeAdapter.addAdapter(convoAdapter);



            setEmptyText("No Communications");
            registerForContextMenu(getListView());
            setListAdapter(mergeAdapter);
            getLoaderManager().initLoader(2, null, this);
            getLoaderManager().initLoader(0, null, this);
        }



        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Intent intent = new Intent();
            String itemId = ((CursorWrapper) mergeAdapter.getItem(position)).getString(0);
            String entry_id;
            if (((CursorWrapper) mergeAdapter.getItem(position)).getColumnName(1).equals(DataContract.Convo.COLUMN_NAME_SUBJECT)){
                entry_id = ((CursorWrapper) mergeAdapter.getItem(position)).getString(4);
                intent.setClass(getActivity(), MsgDetail.class);
            }
            else {
                entry_id = ((CursorWrapper) mergeAdapter.getItem(position)).getString(2);
                intent.setClass(getActivity(), BcDetail.class);
            }
            intent.putExtra("id", itemId);
            intent.putExtra("entry_id", entry_id);
            startActivityForResult(intent, 0);
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
                if (((CursorWrapper) mergeAdapter.getItem(info.position)).getColumnName(1).equals(DataContract.Convo.COLUMN_NAME_SUBJECT)){
                    data.putString(SyncPosts.CONVO_ID, ((CursorWrapper) mergeAdapter.getItem(info.position)).getString(4));
                    new AsyncTask<Bundle, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Bundle... data) {
                            return SyncPosts.putKillConvo(data[0], SyncUtil.getAccount(), getActivity());
                        }

                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            super.onPostExecute(aBoolean);
                            if (aBoolean) {
                                SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_CONVO);
                            } else {
                                AdminUtil.toast(getActivity(), "Failed to Delete");
                            }

                        }
                    }.execute(data);
                }
                else {
                    data.putString(SyncPosts.BROADCAST_ID, ((CursorWrapper) mergeAdapter.getItem(info.position)).getString(2));
                    new AsyncTask<Bundle, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Bundle... data) {
                            return SyncPosts.putKillBroadcast(data[0], SyncUtil.getAccount(), getActivity());
                        }

                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            super.onPostExecute(aBoolean);
                            if (aBoolean) {
                                SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_BC);
                            } else {
                                AdminUtil.toast(getActivity(), "Failed to Delete");
                            }

                        }
                    }.execute(data);
                }
            }
            return super.onContextItemSelected(item);
        }

        public void adapterNotifies(){
            bcAdapter.notifyDataSetChanged();
            convoAdapter.notifyDataSetChanged();
            mergeAdapter.notifyDataSetChanged();
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == 2) {
                return new CursorLoader(getActivity(), DataContract.Broadcast.CONTENT_URI, BC_PROJECTION, null, null, null);
            }
            else if (id == 0){
                return new CursorLoader(getActivity(), DataContract.Convo.CONTENT_URI, PROJECTION, null, null, null);
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (loader.getId() == 0){
                convoAdapter.changeCursor(data);
            }
            else if (loader.getId() == 2){
                bcAdapter.changeCursor(data);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
