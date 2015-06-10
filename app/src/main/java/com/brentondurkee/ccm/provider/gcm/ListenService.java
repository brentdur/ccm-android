package com.brentondurkee.ccm.provider.gcm;

import android.os.Bundle;
import android.util.Log;

import com.brentondurkee.ccm.provider.SyncUtil;
import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by brenton on 6/9/15.
 */
public class ListenService extends GcmListenerService{
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");

        Log.d("GCMRec", "From: " + from);
        Log.d("GCMRec", "Message: " + message);
        if(message.contains("sync")){
            SyncUtil.TriggerRefresh();
        }

    }

}
