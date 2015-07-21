/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
 */

package com.brentondurkee.ccm.provider.gcm;

import android.content.Context;
import android.content.Intent;

import com.brentondurkee.ccm.Log;
import com.google.android.gms.gcm.GcmReceiver;

/**
 * Created by brenton on 6/9/15.
 */
public class Receiver extends GcmReceiver{

    private final String TAG=getClass().getSimpleName();

    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.v(TAG, "Received GCM");
    }
}
