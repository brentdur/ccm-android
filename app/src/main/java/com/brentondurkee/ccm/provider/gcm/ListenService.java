package com.brentondurkee.ccm.provider.gcm;

import android.os.Bundle;
import android.util.Log;

import com.brentondurkee.ccm.provider.SyncUtil;
import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by brenton on 6/9/15.
 */
public class ListenService extends GcmListenerService{

    private final String TAG=getClass().getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("sync");

        Log.d(TAG, "Sync: " + message);
        if(message.contains("all")){
            SyncUtil.TriggerRefresh();
        }
        if(message.contains("events")){
            SyncUtil.TriggerRefresh();
        }
        if(message.contains("messages")){
            SyncUtil.TriggerRefresh();
        }
        if(message.contains("talks")){
            SyncUtil.TriggerRefresh();
        }
        if(message.contains("locations")){
            SyncUtil.TriggerRefresh();
        }
        //TODO: add other messages
        //TODO: add sync based on type (selective sync)

    }

}
