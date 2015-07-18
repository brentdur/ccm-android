package com.brentondurkee.ccm.talks;

import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TalkDetail extends FragmentActivity {

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
                    .add(R.id.container, new TalkDetailFragment())
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

    public static class TalkDetailFragment extends Fragment {

        private TextView reference;
        private TextView fullRef;
        private boolean open = false;
        private TextView openButton;

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
            String date = Utils.dateForm(mCursor.getString(3));
            String reference = mCursor.getString(4);
            String outline = mCursor.getString(5);
            Log.v("Talk Details", outline);
//            outline = "->> " + outline;
            String[] outlist = outline.split("\",,,\"");
            String numbers = "";
            outline = "";
            for(int i = 0; i<outlist.length; i ++){
                outline += outlist[i] + "\n";
                numbers += i+1 + "\n\n";
            }
//            outline = outline.replace("\",,,\"", "\n->> ");

            View rootView = inflater.inflate(R.layout.fragment_talk_detail, container, false);
            ((TextView) rootView.findViewById(R.id.talkDetailTopic)).setText(subject);
            ((TextView) rootView.findViewById(R.id.talkDetailAuthor)).setText(author);
            ((TextView) rootView.findViewById(R.id.talkDetailTime)).setText(date);
            this.reference = (TextView) rootView.findViewById(R.id.talkDetailVerse);
            this.reference.setText(reference);
            this.fullRef = (TextView) rootView.findViewById(R.id.fullVerse);
            this.reference.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickRef();
                }
            });
            ((TextView) rootView.findViewById(R.id.talkDetailOutline)).setText(outline);
//            ((TextView) rootView.findViewById(R.id.talkDetailNumbers)).setText(numbers);
            openButton = (TextView) rootView.findViewById(R.id.openVerse);

            return rootView;
        }

        public void clickRef(){
            if(open){
                openButton.setText(">");
                ObjectAnimator animation = ObjectAnimator.ofInt(fullRef, "maxLines", 0);
                animation.setDuration(300).start();
            } else {
                openButton.setText("V");
                ObjectAnimator animation = ObjectAnimator.ofInt(fullRef, "maxLines", 40);
                animation.setDuration(600).start();

            }
            open = !open;
        }

    }
}
