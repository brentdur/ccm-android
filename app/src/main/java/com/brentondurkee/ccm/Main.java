package com.brentondurkee.ccm;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.brentondurkee.ccm.common.accounts.GenericAccountService;
import com.brentondurkee.ccm.events.Events;
import com.brentondurkee.ccm.inbox.Msgs;
import com.brentondurkee.ccm.provider.DataContract;
import com.brentondurkee.ccm.provider.SyncUtil;
import com.brentondurkee.ccm.provider.gcm.RegIntentService;
import com.brentondurkee.ccm.talks.Talks;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


public class Main extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SyncUtil.CreateSyncAccount(this);


        ((Button) findViewById(R.id.toEvents)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventsIntent = new Intent(v.getContext(), Events.class);
                v.getContext().startActivity(eventsIntent);
            }
        });

        ((Button) findViewById(R.id.toInbox)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver mResolver = v.getContext().getContentResolver();
                mResolver.delete(DataContract.Event.CONTENT_URI, "1", null);
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

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegIntentService.class);
            Log.v("Play Services", "Start Service");
            startService(intent);
        }


    }

    private boolean checkPlayServices() {
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
}

