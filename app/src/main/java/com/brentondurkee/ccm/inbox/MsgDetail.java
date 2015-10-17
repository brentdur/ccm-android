package com.brentondurkee.ccm.inbox;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.brentondurkee.ccm.Log;
import com.brentondurkee.ccm.R;
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
            String convoId = getIntent().getExtras().getString("entry_id");
            Bundle data = new Bundle();
            final Context context = this;
            data.putString(SyncPosts.CONVO_ID, convoId);
            new AsyncTask<Bundle, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Bundle... data) {
                    return SyncPosts.putKillConvo(data[0], SyncUtil.getAccount(), context);
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    super.onPostExecute(aBoolean);
                    if (aBoolean) {
                        SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_CONVO);
                        finishActivity(0);
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
    public static class MsgDetailFragment extends Fragment  {

        final String[] PROJECTION = new String[]{
                DataContract.Convo.COLUMN_NAME_SUBJECT,
                DataContract.Convo.COLUMN_NAME_MESSAGES,
                DataContract.Convo.COLUMN_NAME_MINMESSAGES,
                DataContract.Convo.COLUMN_NAME_FROM,
                DataContract.Convo.COLUMN_NAME_SINGLETON
        };

        String subject;
        String messages;
        String minMessages;
        String from;

        String[] mess;
        String[] minMess;

        Cursor mCursor;

        ContentObserver contentObserver;

        ScrollView scroller;

        public MsgDetailFragment() {
        }

        public void updateBlocks(View rootView){
            Log.v("Message Cycle", "Updating Blocks");
            final RelativeLayout holder = (RelativeLayout) rootView.findViewById(R.id.msgListHolder);


            holder.removeAllViews();

            FrameLayout.LayoutParams holderParams = (FrameLayout.LayoutParams) holder.getLayoutParams();
            holderParams.width = scroller.getLayoutParams().width;
            holder.setLayoutParams(holderParams);

            mess = messages.split("(%BREAK%)");
            minMess = minMessages.split("(%BREAK%)");
            Log.v("Message", "Message: " + messages);
            int mmi = 0;
            View prev = null;
            for (int i = 0; i< mess.length; i++){
                String cur = mess[i];
                Log.v("Message", "Message: " + mess[i]);
                View msg;
                if (cur.equals("%OTHER%")) {
                    msg = getActivity().getLayoutInflater().inflate(R.layout.convo_text_1, holder, false);
                    ((TextView) msg.findViewById(R.id.text)).setText(minMess[mmi++]);
                }
                else {
                    msg = getActivity().getLayoutInflater().inflate(R.layout.convo_text_2, holder, false);
                    ((TextView) msg.findViewById(R.id.text)).setText(mess[i]);
                }
                msg.setId(i+1);
                RelativeLayout.LayoutParams msgParams = (RelativeLayout.LayoutParams) msg.getLayoutParams();

                if (prev != null){
                    msgParams.addRule(RelativeLayout.BELOW, i);
                }

                holder.addView(msg, msgParams);
                prev = msg;
            }
        }

        public void buildBlock(View rootView, String text) {
            Log.v("Message Cycle", "Building Blocks");
            final ScrollView scroller = (ScrollView) rootView.findViewById(R.id.msgsList);
            final RelativeLayout holder = (RelativeLayout) rootView.findViewById(R.id.msgListHolder);
            int prevId = holder.findViewById(mess.length).getId();
            View msg = getActivity().getLayoutInflater().inflate(R.layout.convo_text_2, holder, false);
            ((TextView) msg.findViewById(R.id.text)).setText(text);
            msg.setId(prevId + 1);
            RelativeLayout.LayoutParams msgParams = (RelativeLayout.LayoutParams) msg.getLayoutParams();
            msgParams.addRule(RelativeLayout.BELOW, prevId);
            holder.addView(msg, msgParams);
        }

        public void callCursor(String id){
            Log.v("Message Cycle", "Calling Cursor");
            mCursor = getActivity().getContentResolver().query(DataContract.Convo.CONTENT_URI, PROJECTION, DataContract.Convo._ID + "='" + id + "'", null, null);
            DataSetObserver dataSetObserver = new DataSetObserver() {
                @Override
                public void onChanged() {
                    Log.v("Data Set", "Changed");
                    super.onChanged();
                }

                @Override
                public void onInvalidated() {
                    Log.v("Data Set", "Invalidated");
                    super.onInvalidated();
                }
            };

            mCursor.registerDataSetObserver(dataSetObserver);
            mCursor.moveToFirst();
            subject = mCursor.getString(0);
            messages = mCursor.getString(1);
            minMessages = mCursor.getString(2);
            from = mCursor.getString(3);
        }

        public void reset(View rootView, String id){
            Log.v("Message Cycle", "resetting");
            ((TextView)rootView.findViewById(R.id.msgText)).setText("");
            callCursor(id);
            updateBlocks(rootView);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle extras = getActivity().getIntent().getExtras();
            final String id = extras.getString("id");
            final String entryId = extras.getString("entry_id");
            callCursor(id);


            final View rootView = inflater.inflate(R.layout.fragment_convo_detail, container, false);
            scroller = (ScrollView) rootView.findViewById(R.id.msgsList);

            scroller.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    scroller.post(new Runnable() {
                        public void run() {
                            scroller.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                }
            });

            contentObserver = new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    reset(rootView, id);
                    super.onChange(selfChange);
                }
            };

            updateBlocks(rootView);

            final boolean isSingleton = mCursor.getInt(4) == 1;
            if (mCursor.getInt(4) == 1) {
                rootView.findViewById(R.id.sendButton).setEnabled(false);
                ((TextView) rootView.findViewById(R.id.msgText)).setHint("Message is one way only");
                rootView.findViewById(R.id.msgText).setEnabled(false);
            }

            ((Button) rootView.findViewById(R.id.sendButton)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isSingleton) {
                        return;
                    }
                    final String message = ((TextView) rootView.findViewById(R.id.msgText)).getText().toString();
                    if (message.isEmpty()) {
                        AdminUtil.toast(getActivity(), "Write a message");

                        return;
                    }
                    Bundle data = new Bundle();
                    data.putString(SyncPosts.CONVO_ID, entryId);
                    data.putString(SyncPosts.CONVO_MESSAGE, message);
                    AdminUtil.showDialog(getActivity());
                    new AsyncTask<Bundle, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Bundle... data) {
                            return SyncPosts.putSendConvoMsg(data[0], SyncUtil.getAccount(), getActivity());
                        }

                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            super.onPostExecute(aBoolean);
                            AdminUtil.hideDialog();
                            if (aBoolean) {
                                SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_CONVO);
                                AdminUtil.toast(getActivity().getApplicationContext(), "Message Sent");
                                buildBlock(rootView, message);
                                reset(rootView, id);
                                getActivity().getContentResolver().registerContentObserver(mCursor.getNotificationUri(), false, contentObserver);

                            } else {
                                AdminUtil.toast(getActivity().getApplicationContext(), "Failed to Send Message");
                            }

                        }
                    }.execute(data);
                }
            });

            ((TextView) rootView.findViewById(R.id.convoDetailSubject)).setText(subject);

            return rootView;
        }

        @Override
        public void onDestroy() {
            getActivity().getContentResolver().unregisterContentObserver(contentObserver);
            mCursor.close();
            super.onPause();
        }
    }


}
