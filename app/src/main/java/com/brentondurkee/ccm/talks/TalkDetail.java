package com.brentondurkee.ccm.talks;

import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.brentondurkee.ccm.Log;
import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.Utils;
import com.brentondurkee.ccm.provider.DataContract;

public class TalkDetail extends AppCompatActivity {

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
                    .add(R.id.container, new TalkDetailFragment())
                    .commit();
        }
    }


    public static class TalkDetailFragment extends Fragment {

        private TextView reference;
        private TextView fullRef;
        private boolean open = false;
        private ImageButton openButton;

        Cursor mCursor;
        ContentResolver mResolve;
        final String[] PROJECTION = new String[]{
                DataContract.Talk._ID,
                DataContract.Talk.COLUMN_NAME_SUBJECT,
                DataContract.Talk.COLUMN_NAME_AUTHOR,
                DataContract.Talk.COLUMN_NAME_DATE,
                DataContract.Talk.COLUMN_NAME_REFERENCE,
                DataContract.Talk.COLUMN_NAME_VERSE,
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
            String verse = mCursor.getString(5);
            String outline = mCursor.getString(6);
            Log.v("Talk Details", outline);

            View rootView = inflater.inflate(R.layout.fragment_talk_detail, container, false);
            ((TextView) rootView.findViewById(R.id.talkDetailTopic)).setText(subject);
            ((TextView) rootView.findViewById(R.id.talkDetailAuthor)).setText(author);
            ((TextView) rootView.findViewById(R.id.talkDetailTime)).setText(date);
            this.reference = (TextView) rootView.findViewById(R.id.talkDetailVerse);
            this.reference.setText(reference);
            this.fullRef = (TextView) rootView.findViewById(R.id.talkDetailFullVerse);
            fullRef.setText(verse);
            fullRef.setMaxLines(0);
            this.reference.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickRef();
                }
            });
            ((TextView) rootView.findViewById(R.id.talkDetailOutline)).setText(outline);
            openButton = (ImageButton) rootView.findViewById(R.id.openVerse);

            return rootView;
        }

        public void clickRef(){
            if(open){
                openButton.setImageResource(R.drawable.downarrow);
                ObjectAnimator animation = ObjectAnimator.ofInt(fullRef, "maxLines", 0);
                animation.setDuration(300).start();
            } else {
                openButton.setImageResource(R.drawable.uparrow);
                ObjectAnimator animation = ObjectAnimator.ofInt(fullRef, "maxLines", 40);
                animation.setDuration(600).start();

            }
            open = !open;
        }

    }
}
