package com.brentondurkee.ccm.inbox;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.Utils;
import com.brentondurkee.ccm.provider.DataContract;
import com.brentondurkee.ccm.provider.SyncUtil;


/**
 * Created by brenton on 6/12/15.
 *
 * Detail fragment for Messages
 */
public class MsgDetail extends FragmentActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.primaryCCM));
        toolbar.setTitleTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
        setActionBar(toolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MsgDetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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
            SyncUtil.TriggerRefresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MsgDetailFragment extends Fragment {

        final String[] PROJECTION = new String[]{
            DataContract.Msg.COLUMN_NAME_FROM,
                DataContract.Msg.COLUMN_NAME_TO,
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
            String to = mCursor.getString(1);
            String subject = mCursor.getString(2);
            String date = Utils.dateForm(mCursor.getString(3));
            String message = mCursor.getString(4);

            View rootView = inflater.inflate(R.layout.fragment_msg_detail, container, false);
            ((TextView) rootView.findViewById(R.id.msgDetailSubject)).setText(subject);
            ((TextView) rootView.findViewById(R.id.msgDetailFrom)).setText(from);
            ((TextView) rootView.findViewById(R.id.msgDetailTo)).setText(to);
            ((TextView) rootView.findViewById(R.id.msgDetailTime)).setText(date);
            ((TextView) rootView.findViewById(R.id.msgDetailMsg)).setText(message);
            mCursor.close();
            return rootView;
        }
    }
}
