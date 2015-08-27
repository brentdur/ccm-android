package com.brentondurkee.ccm.inbox;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.Utils;
import com.brentondurkee.ccm.admin.AdminUtil;
import com.brentondurkee.ccm.provider.DataContract;
import com.brentondurkee.ccm.provider.SyncPosts;
import com.brentondurkee.ccm.provider.SyncUtil;


/**
 * Created by brenton on 6/12/15.
 *
 * Detail fragment for Messages
 */
public class MsgDetail extends AppCompatActivity {


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
                    .add(R.id.container, new MsgDetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mInflate = getMenuInflater();
        mInflate.inflate(R.menu.msg_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete_msg){
            String msgId = getIntent().getExtras().getString("entry_id");
            Bundle data = new Bundle();
            final Context context = this;
            data.putString(SyncPosts.DELETE_MESSAGE, msgId);
            new AsyncTask<Bundle, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Bundle... data) {
                    return SyncPosts.deleteMsg(data[0], SyncUtil.getAccount(), context);
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    super.onPostExecute(aBoolean);
                    if (aBoolean) {
                        SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_MSG);
                        finish();
                    } else {
                        AdminUtil.toast(getApplicationContext(), "Failed to Delete");
                    }

                }
            }.execute(data);
        }
        if (id == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MsgDetailFragment extends Fragment {

        final String[] PROJECTION = new String[]{
                DataContract.Msg.COLUMN_NAME_SIMPLE_FROM,
                DataContract.Msg.COLUMN_NAME_SUBJECT,
                DataContract.Msg.COLUMN_NAME_DATE,
                DataContract.Msg.COLUMN_NAME_MESSAGE
        };

        public MsgDetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle extras = getActivity().getIntent().getExtras();
            String id = extras.getString("id");
            Cursor mCursor = getActivity().getContentResolver().query(DataContract.Msg.CONTENT_URI, PROJECTION, DataContract.Msg._ID + "='" + id + "'", null, null);
            mCursor.moveToFirst();
            String from = mCursor.getString(0);
            String subject = mCursor.getString(1);
            String date = Utils.dateForm(mCursor.getString(2));
            String message = mCursor.getString(3);

            View rootView = inflater.inflate(R.layout.fragment_msg_detail, container, false);
            ((TextView) rootView.findViewById(R.id.msgDetailSubject)).setText(subject);
            if (from.isEmpty()){
                from = "Anonymous";
            }
            ((TextView) rootView.findViewById(R.id.msgDetailFrom)).setText(from);
            ((TextView) rootView.findViewById(R.id.msgDetailTime)).setText(date);
            ((TextView) rootView.findViewById(R.id.msgDetailMsg)).setText(message);
            mCursor.close();
            return rootView;
        }
    }
}
