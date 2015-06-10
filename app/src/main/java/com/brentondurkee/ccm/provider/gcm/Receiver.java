package com.brentondurkee.ccm.provider.gcm;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GcmReceiver;

/**
 * Created by brenton on 6/9/15.
 */
public class Receiver extends GcmReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("OnReceive", "Recieved");
    }
}
