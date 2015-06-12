package com.brentondurkee.ccm;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

import com.brentondurkee.ccm.auth.AuthUtil;
import com.brentondurkee.ccm.common.accounts.GenericAccountService;
import com.brentondurkee.ccm.events.Events;
import com.brentondurkee.ccm.inbox.Msgs;
import com.brentondurkee.ccm.provider.DataContract;
import com.brentondurkee.ccm.provider.SyncUtil;
import com.brentondurkee.ccm.provider.gcm.RegIntentService;
import com.brentondurkee.ccm.talks.Talks;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


public class Main extends FragmentActivity{

    private final String TAG=getClass().getSimpleName();

    public static String PREF_ACCOUNT_EMAIL="account_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SyncUtil.mainContext = this;

//        final ActionBar actionBar = getSupportActionBar();
//        actionBar.hide();

        Log.v(TAG, "Create Activity");
        Log.v(TAG, "Test token: " + SyncUtil.getAuthToken());


        ((Button) findViewById(R.id.toEvents)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventsIntent = new Intent(v.getContext(), Pager.class);
                v.getContext().startActivity(eventsIntent);
            }
        });

        ((Button) findViewById(R.id.toInbox)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventsIntent = new Intent(v.getContext(), Msgs.class);
                v.getContext().startActivity(eventsIntent);
            }
        });

        ((Button) findViewById(R.id.toTalks)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventsIntent = new Intent(v.getContext(), Talks.class);
                v.getContext().startActivity(eventsIntent);
            }
        });

        if (!testAccount()) {
            Log.v(TAG, "No Account Found");
            addAccount();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflate = getMenuInflater();
        inflate.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
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
        if(PreferenceManager.getDefaultSharedPreferences(this).contains(PREF_ACCOUNT_EMAIL)){
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
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "Stop Activity");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "Resume Activity");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "Pause Activity");
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "Destroy Activity");
        SyncUtil.flush();
        super.onDestroy();
    }
}

