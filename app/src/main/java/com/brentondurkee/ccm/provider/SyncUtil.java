/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.brentondurkee.ccm.provider;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.brentondurkee.ccm.Log;

/**
 * Static helper methods for working with the sync framework.
 */
public class SyncUtil {
    private static final String TAG="SyncUtil";
    private static final long SYNC_FREQUENCY = 60 * 60;  // 1 hour (in seconds)
    private static final String CONTENT_AUTHORITY = DataContract.CONTENT_AUTHORITY;
    // Value below must match the account type specified in res/xml/syncadapter.xml
    public final static String PREF_ACCOUNT_EMAIL="account_email";
    public final static String PREF_CAN_SIGNUPS = "can_write_signups";
    public final static String PREF_CAN_EVENTS = "can_write_events";
    public final static String PREF_CAN_TALKS = "can_write_talks";
    public final static String PREF_IS_MINISTER = "is_minister";

    public final static String SELECTIVE_KEY = "selective";
    public final static String SELECTION = "selection";

    public final static String SELECTIVE_SIGNUP = "signup";
    public final static String SELECTIVE_MSG = "message";
    public final static String SELECTIVE_EVENT = "event";
    public final static String SELECTIVE_TALK = "talk";
    public final static String SELECTIVE_LOCATION = "location";
    public final static String SELECTIVE_GROUP = "group";
    public final static String SELECTIVE_TOPIC = "topic";

    public static Context mainContext;
    private static Account mAccount;
    private static String authToken;

    public static void addAuthToken(String token){
        Log.v(TAG, "Add Token");
        authToken = token;
    }

    public static String getAuthToken(){
        Log.v(TAG, "Get Token");
        return authToken;
    }

    public static void addAccount(Account account, boolean isFirst){
        mAccount = account;
        Log.v(TAG, "Add Account");
        if(isFirst) {
            Log.v(TAG, "Add First");
            ContentResolver.setIsSyncable(mAccount, CONTENT_AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(mAccount, CONTENT_AUTHORITY, true);
            ContentResolver.addPeriodicSync(mAccount, CONTENT_AUTHORITY, new Bundle(), SYNC_FREQUENCY);
            PreferenceManager.getDefaultSharedPreferences(mainContext).edit().putString(PREF_ACCOUNT_EMAIL, mAccount.name).commit();
            TriggerRefresh();
        }


    }

    public static Account getAccount(){
        return mAccount;
    }

    public static void flush(){
        Log.v(TAG, "Flushing");
        mAccount=null;
        authToken=null;
    }

    public static Void syncDone() {
        Log.v(TAG, "Done");
        compareGroups(SyncPosts.getMe(null, getAccount(), mainContext));
        return null;
    }

    public static void compareGroups(Bundle data){
        SharedPreferences.Editor manager = PreferenceManager.getDefaultSharedPreferences(mainContext).edit();
        manager.putBoolean(PREF_CAN_SIGNUPS, data.getBoolean(SyncPosts.ME_SIGNUPS_KEY, false));
        manager.putBoolean(PREF_CAN_EVENTS, data.getBoolean(SyncPosts.ME_EVENTS_KEY, false));
        manager.putBoolean(PREF_CAN_TALKS, data.getBoolean(SyncPosts.ME_TALKS_KEY, false));
        manager.putBoolean(PREF_IS_MINISTER, data.getBoolean(SyncPosts.ME_MINISTERS_KEY, false));
        manager.commit();
    }



    /**
     * Helper method to trigger an immediate sync ("refresh").
     *
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     *
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     */
    public static void TriggerRefresh() {
        Bundle b = new Bundle();
        Log.v("Refresh", "refreshing");
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                mAccount, // Sync account
                DataContract.CONTENT_AUTHORITY,                 // Content authority
                b);                                             // Extras
    }

    public static void TriggerSelectiveRefresh(String selection) {
        Bundle b = new Bundle();
        Log.v("Refresh", "refreshing");
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        b.putBoolean(SELECTIVE_KEY, true);
        b.putString(SELECTION, selection);
        ContentResolver.requestSync(
                mAccount, // Sync account
                DataContract.CONTENT_AUTHORITY,                 // Content authority
                b);                                             // Extras
    }
}
