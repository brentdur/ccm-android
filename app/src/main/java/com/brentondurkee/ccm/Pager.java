package com.brentondurkee.ccm;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.graphics.Color;
import android.os.Bundle;

import android.support.design.widget.TabLayout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.support.v7.app.ActionBarActivity;
//import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

import com.brentondurkee.ccm.admin.AddEvent;
import com.brentondurkee.ccm.admin.AddMsg;
import com.brentondurkee.ccm.admin.AddTalk;
import com.brentondurkee.ccm.auth.AuthUtil;
import com.brentondurkee.ccm.events.EventList;
import com.brentondurkee.ccm.inbox.MsgList;
import com.brentondurkee.ccm.provider.SyncUtil;
import com.brentondurkee.ccm.provider.gcm.RegIntentService;
import com.brentondurkee.ccm.talks.TalkList;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.zip.Inflater;

/**
 * Created by brenton on 6/11/15.
 */
public class Pager extends FragmentActivity {
    CollectionPagerActivity mAdapter;
    ViewPager mPager;
    private final String TAG=getClass().getSimpleName();
    public static String PREF_ACCOUNT_EMAIL="account_email";
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_pager);

        SyncUtil.mainContext = this;

        Log.v(TAG, "Create Activity");
        Log.v(TAG, "Test token: " + SyncUtil.getAuthToken());


        if (!testAccount()) {
            Log.v(TAG, "No Account Found");
            addAccount();
        }

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setActionBar(toolbar);
//        setSupportActionBar(toolbar);


        mAdapter = new CollectionPagerActivity(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(1);
        mPager.setPageMargin(25);
        Log.v(TAG, "created");

        TabLayout tabLayout = (TabLayout) findViewById(R.id.frame);
        tabLayout.setupWithViewPager(mPager);
        tabLayout.setTabTextColors(Color.WHITE, Color.BLACK);
        tabLayout.getTabAt(1).select();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mInfalte = getMenuInflater();
        mInfalte.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        Intent openA;
        if (id == R.id.action_settings) {
            SyncUtil.TriggerRefresh();
            return true;
        }
        else if (id == R.id.add_event) {
            openA = new Intent(getBaseContext(), AddEvent.class);
        }
        else if (id == R.id.add_talk) {
            openA = new Intent(getBaseContext(), AddTalk.class);
        }
        else{
            openA = new Intent(getBaseContext(), AddMsg.class);
        }
        startActivity(openA);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.v(TAG, "Start Activity");
        Log.v(TAG, "Test token: " + SyncUtil.getAuthToken());

//        if(!testAccount()){
//            Log.v(TAG, "No Account Found");
//            addAccount();
//        }

    }

    private boolean testAccount(){
        Log.v(TAG, "Test Account");
        AccountManager am = AccountManager.get(this);
        boolean found = false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.contains(PREF_ACCOUNT_EMAIL)){
            Account[] accounts = am.getAccountsByType(AuthUtil.ACCOUNT_TYPE);
            for(int i = 0; i<accounts.length; i++){
                Log.v(TAG, "Account: " + accounts[i].name);
                if(accounts[i].name.equals(PreferenceManager.getDefaultSharedPreferences(this).getString("account_email", ""))){
                    found = true;
                    Log.v(TAG, "Account Found: " + accounts[i].toString());
                    SyncUtil.addAccount(accounts[i], false);
                    getAuthToken(accounts[i]);
                    break;
                }
            }
        }
        return found;
    }

    public void addAccount(){
        Log.v(TAG, "Add Account");
        final AccountManagerFuture<Bundle> future = AccountManager.get(this).addAccount(AuthUtil.ACCOUNT_TYPE, AuthUtil.TOKEN_TYPE_ACCESS, null, null, this, new AccountManagerCallback<Bundle>() {

            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle ret = future.getResult();
                    Account account = new Account(ret.getString(AccountManager.KEY_ACCOUNT_NAME), ret.getString(AccountManager.KEY_ACCOUNT_TYPE));
                    SyncUtil.addAccount(account, true);
                    getAuthToken(account);
                }
                catch(OperationCanceledException e){
                    Log.v(TAG, "Cancelled LogIn");
                    addAccount();
                }
                catch(Exception e){
                    e.printStackTrace();
                    Log.w(TAG, "Threw exceptions: " + e.toString());
                }
            }

        }, null);
    }

    public void getAuthToken(Account account){
        Log.v(TAG, "Get Auth Token");
        AccountManagerFuture<Bundle> future = AccountManager.get(this).getAuthToken(account, AuthUtil.TOKEN_TYPE_ACCESS, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Log.v(TAG, "Prepare to add Auth");
                    Bundle ret = future.getResult();
                    SyncUtil.addAuthToken(ret.getString(AccountManager.KEY_AUTHTOKEN));
                    doGCM();
                }
                catch(OperationCanceledException e){
                    Log.v(TAG, "Cancelled LogIn");
                    addAccount();
                }
                catch(Exception e){
                    e.printStackTrace();
                    Log.w(TAG, "Threw exceptions: " + e.toString());
                }
            }
        }, null);
    }

    private void doGCM() {
        Log.v(TAG, "Start GCM");
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegIntentService.class);
            startService(intent);
        }
    }


    private boolean checkPlayServices() {
        Log.v(TAG, "Check Play Services");
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        9000).show();
            } else {
                Log.i("Check", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        SyncUtil.flush();
        super.onDestroy();
    }
}

class CollectionPagerActivity extends FragmentPagerAdapter {
    private final String TAG=getClass().getSimpleName();

    public CollectionPagerActivity(FragmentManager fm) {
        super(fm);
    }

    public void setTabLayout(int index, TabLayout.Tab Tab, LayoutInflater inflater, ViewPager pager){
        View view = inflater.inflate(R.layout.tab, pager, false);
//        ((Button) view.findViewById(R.id.tab)).setText(getPageTitle(index));
        Tab.setCustomView(view);
    }

    @Override
    public CharSequence getPageTitle(int position) {
//        return super.getPageTitle(position);
        switch(position){
            case 1: return "Events";
            case 0: return "Messages";
            case 2: return "Talks";
            default: return "Default";
        }
    }

    @Override
    public Fragment getItem(int position) {
        Log.v(TAG, "get item: " + position);
        switch(position){
            case 1: return new EventList();
            case 0: return new MsgList();
            case 2: return new TalkList();
            default: Log.v(TAG, "Default"); return null;

        }
    }

    @Override
    public int getCount() {
        return 3;
    }


}
