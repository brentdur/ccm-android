package com.brentondurkee.ccm.provider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

import com.brentondurkee.ccm.Log;
import com.brentondurkee.ccm.Utils;
import com.brentondurkee.ccm.auth.AuthUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by brenton on 6/8/15.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    final String TAG = "SyncAdapter";
    final String[] EVENT_PROJECTION = new String[]{
            DataContract.Event.COLUMN_NAME_ENTRY_ID,
            DataContract.Event.COLUMN_NAME_VERSION,
            DataContract.Event._ID};
    final String[] TALK_PROJECTION = new String[]{
            DataContract.Talk.COLUMN_NAME_ENTRY_ID,
            DataContract.Talk.COLUMN_NAME_VERSION,
            DataContract.Talk._ID};
    final String[] LOCATION_PROJECTION = new String[]{
            DataContract.Location.COLUMN_NAME_ENTRY_ID,
            DataContract.Location.COLUMN_NAME_VERSION,
            DataContract.Location._ID};
    final String[] GROUP_PROJECTION = new String[]{
            DataContract.Group.COLUMN_NAME_ENTRY_ID,
            DataContract.Group.COLUMN_NAME_VERSION,
            DataContract.Group._ID};
    final String[] SIGNUP_PROJECTION = new String[]{
            DataContract.Signup.COLUMN_NAME_ENTRY_ID,
            DataContract.Signup.COLUMN_NAME_VERSION,
            DataContract.Signup._ID};
    final String[] TOPIC_PROJECTION = new String[]{
            DataContract.Topic.COLUMN_NAME_ENTRY_ID,
            DataContract.Topic.COLUMN_NAME_VERSION,
            DataContract.Topic._ID};
    final String[] CONVO_PROJECTION = new String[]{
            DataContract.Convo.COLUMN_NAME_ENTRY_ID,
            DataContract.Convo.COLUMN_NAME_VERSION,
            DataContract.Convo._ID};
    final String[] BROADCAST_PROJECTION = new String[]{
            DataContract.Broadcast.COLUMN_NAME_ENTRY_ID,
            DataContract.Broadcast.COLUMN_NAME_VERSION,
            DataContract.Broadcast._ID};

    private final String eventFeed = Utils.DOMAIN + "/api/events";
    private final String talkFeed = Utils.DOMAIN + "/api/talks";
    private final String locationFeed = Utils.DOMAIN + "/api/locations";
    private final String groupFeed = Utils.DOMAIN + "/api/groups";
    private final String signupFeed = Utils.DOMAIN + "/api/signups";
    private final String topicFeed = Utils.DOMAIN + "/api/topics";
    private final String convoFeed = Utils.DOMAIN + "/api/conversations/mine";
    private final String convoMinisterFeed = Utils.DOMAIN + "/api/conversations/minister";
    private final String bcFeed = Utils.DOMAIN + "/api/broadcasts/mine";

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSync) {
        super(context, autoInitialize, allowParallelSync);

    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.v(TAG, "starting sync");
        String token = AccountManager.get(getContext()).peekAuthToken(account, AuthUtil.TOKEN_TYPE_ACCESS);
        if (extras.getBoolean(SyncUtil.SELECTIVE_KEY, false)) {
            Log.v(TAG, "selective sync");
            if(extras.getString(SyncUtil.SELECTION, "").equals(SyncUtil.SELECTIVE_SIGNUP)){
                sync(signupFeed, DataContract.Signup.CONTENT_URI, SIGNUP_PROJECTION, "signup", token);
            }
            else if(extras.getString(SyncUtil.SELECTION, "").equals(SyncUtil.SELECTIVE_EVENT)){
                sync(eventFeed, DataContract.Event.CONTENT_URI, EVENT_PROJECTION, "event", token);
            }
            else if(extras.getString(SyncUtil.SELECTION, "").equals(SyncUtil.SELECTIVE_TALK)){
                sync(talkFeed, DataContract.Talk.CONTENT_URI, TALK_PROJECTION, "talk", token);
            }
            else if(extras.getString(SyncUtil.SELECTION, "").equals(SyncUtil.SELECTIVE_LOCATION)){
                sync(locationFeed, DataContract.Location.CONTENT_URI, LOCATION_PROJECTION, "location", token);
            }
            else if(extras.getString(SyncUtil.SELECTION, "").equals(SyncUtil.SELECTIVE_GROUP)){
                sync(groupFeed, DataContract.Group.CONTENT_URI, GROUP_PROJECTION, "group", token);
            }
            else if(extras.getString(SyncUtil.SELECTION, "").equals(SyncUtil.SELECTIVE_TOPIC)){
                sync(topicFeed, DataContract.Topic.CONTENT_URI, TOPIC_PROJECTION, "topic", token);
            }
            else if(extras.getString(SyncUtil.SELECTION, "").equals(SyncUtil.SELECTIVE_CONVO)){
                //TODO determine if it needs the minster feed or not
                sync(convoFeed, DataContract.Convo.CONTENT_URI, CONVO_PROJECTION, "convo", token);
            }
            else if(extras.getString(SyncUtil.SELECTION, "").equals(SyncUtil.SELECTIVE_BC)){
                sync(bcFeed, DataContract.Broadcast.CONTENT_URI, BROADCAST_PROJECTION, "broadcast", token);
            }
            return;
        }
        sync(eventFeed, DataContract.Event.CONTENT_URI, EVENT_PROJECTION, "event", token);
        sync(talkFeed, DataContract.Talk.CONTENT_URI, TALK_PROJECTION, "talk", token);
        sync(locationFeed, DataContract.Location.CONTENT_URI, LOCATION_PROJECTION, "location", token);
        sync(groupFeed, DataContract.Group.CONTENT_URI, GROUP_PROJECTION, "group", token);
        sync(signupFeed, DataContract.Signup.CONTENT_URI, SIGNUP_PROJECTION, "signup", token);
        sync(topicFeed, DataContract.Topic.CONTENT_URI, TOPIC_PROJECTION, "topic", token);
        sync(convoFeed, DataContract.Convo.CONTENT_URI, CONVO_PROJECTION, "convo", token);
        sync(bcFeed, DataContract.Broadcast.CONTENT_URI, BROADCAST_PROJECTION, "broadcast", token);

    }

    public void sync(String feed, Uri content, String[] projection, String type, String token){
        InputStream stream = null;
        Log.v(TAG, "started for " + type);
        try{
            URL eventURL = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) eventURL.openConnection();
            conn.addRequestProperty("Authorization", "Bearer " + token);
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            Log.v(TAG, "connected: " + feed);
            try {

                int response = conn.getResponseCode();
                Log.v(TAG, "response: "+response);
                if (response == 200) {
                    stream = new BufferedInputStream(conn.getInputStream());
                    Log.v(TAG, "start update");
                    Scanner reader = new Scanner(stream);
                    StringBuilder string = new StringBuilder();
                    do {
                        string.append(reader.nextLine());
                    } while(reader.hasNextLine());
                    Log.d(TAG, "Raw Json: "+string.toString());
                    updateDatabase(string.toString(), content, projection, type);
                    reader.close();
                }
            }
            finally {
                if(stream != null) {
                    stream.close();
                    Log.v(TAG, "close stream");
                }
            }
        }
        catch(MalformedURLException e){
            Log.w(TAG, "Malformed URL");
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
        }
        catch(JSONException e){
            Log.w(TAG, "JSONException: "+ e.toString());
        }
        catch(RemoteException e){
            Log.w(TAG, "RemoteException: "+ e.toString());
        }
        catch(OperationApplicationException e){
            Log.w(TAG, "OperationAppException: "+ e.toString());
        }
    }

    public void updateDatabase(String data, Uri content, String[] projection, String type) throws JSONException, RemoteException, OperationApplicationException{
        JSONArray json = new JSONArray(data);
        ContentResolver mResolver = getContext().getContentResolver();
        ArrayList<JSONObject> objects = new ArrayList<>(json.length());
        int updates = 0;
        int deletes = 0;
        int creates = 0;

        for(int i = 0; i<json.length(); i++){
            objects.add(json.getJSONObject(i));
        }

        ArrayList<ContentProviderOperation> batch = new ArrayList<>(json.length());
        Cursor cursor = mResolver.query(content, projection, null, null, null);
        while(cursor.moveToNext()){
            boolean found = false;
            for(int i = 0; i<objects.size(); i++){
                if(cursor.getString(0).equals(objects.get(i).getString("_id"))){
                    found = true;
                    if(objects.get(i).getInt("version") > cursor.getInt(1)){
                        Uri existing = content.buildUpon().appendPath(Integer.toString(cursor.getInt(2))).build();
                        batch.add(update(existing, objects.get(i), type));
                        updates++;
                    }
                    objects.remove(i);
                }
            }
            if(!found){
                Uri existing = content.buildUpon().appendPath(Integer.toString(cursor.getInt(2))).build();
                batch.add(ContentProviderOperation.newDelete(existing).build());
                deletes++;
            }
        }
        cursor.close();


        for(int i = 0; i < objects.size(); i++){
            batch.add(add(content, objects.get(i), type));
            creates++;
        }
        mResolver.applyBatch(DataContract.CONTENT_AUTHORITY, batch);
        mResolver.notifyChange(content, null, false);
        Log.i(TAG, "Creates " + creates);
        Log.i(TAG, "Deletes " + deletes);
        Log.i(TAG, "Updates " + updates);
    }

    public ContentProviderOperation update(Uri content, JSONObject object, String type) throws JSONException {
        if(type.equals("event")){
            return updateEvent(content,
                    object.getString("title"),
                    object.getString("location"),
                    object.getString("date"),
                    object.getString("description"),
                    object.getDouble("lat"),
                    object.getDouble("lng"),
                    object.getInt("version"));
        }
        if(type.equals("talk")){
            return updateTalk(content,
                    object.getString("author"),
                    object.getString("subject"),
                    object.getString("date"),
                    object.getString("reference"),
                    object.getString("fullVerse"),
                    object.getJSONArray("outline"),
                    object.getInt("version"));
        }
        if(type.equals("group")){
            return updateGroup(content,
                    object.getString("name"),
                    object.getString("writeTalks"),
                    object.getString("writeSignups"),
                    object.getString("writeEvents"),
                    object.getInt("version"));
        }
        if(type.equals("location")){
            return updateLocation(content,
                    object.getString("name"),
                    object.getString("address"),
                    object.getDouble("lat"),
                    object.getDouble("lng"),
                    object.getInt("version"));
        }
        if(type.equals("topic")){
            return updateTopic(content,
                    object.getString("name"),
                    object.getString("isAnon"),
                    object.getInt("version"));
        }
        if(type.equals("signup")){
            return updateSignup(content,
                    object.getString("name"),
                    object.getString("dateInfo"),
                    object.getString("location"),
                    object.getString("address"),
                    object.getString("description"),
                    object.getInt("memberCount"),
                    object.getString("isMemberOf"),
                    object.getInt("version"));
        }
        //TODO Figure this out
//        if(type.equals("convo")){
//            JSONObject participant = object.getJSONObject("participant");
//            return updateConvo(content,
//                    object.getString("pa"),
//                    object.getString("simpleFrom"),
//                    object.getString("simpleTo"),
//                    object.getString("subject"),
//                    object.getString("date"),
//                    participant.getString("user"),
//                    object.getString("message"),
//                    object.getInt("version"));
//        }
        if(type.equals("broadcast")){
            return updateBC(content,
                    object.getString("title"),
                    object.getString("message"),
                    object.getInt("version"));
        }
        return null;
    }

    public ContentProviderOperation add(Uri content, JSONObject object, String type) throws JSONException {
        if(type.equals("event")){
            return addEvent(content,
                    object.getString("title"),
                    object.getString("location"),
                    object.getString("date"),
                    object.getString("description"),
                    object.getDouble("lat"),
                    object.getDouble("lng"),
                    object.getString("_id"),
                    object.getInt("version"));
        }
        if(type.equals("talk")){
            return addTalk(content,
                    object.getString("author"),
                    object.getString("subject"),
                    object.getString("date"),
                    object.getString("reference"),
                    object.getString("fullVerse"),
                    object.getJSONArray("outline"),
                    object.getString("_id"),
                    object.getInt("version"));
        }
        if(type.equals("group")){
            return addGroup(content,
                    object.getString("name"),
                    object.getString("writeTalks"),
                    object.getString("writeSignups"),
                    object.getString("writeEvents"),
                    object.getString("_id"),
                    object.getInt("version"));
        }
        if(type.equals("location")){
            return addLocation(content,
                    object.getString("name"),
                    object.getString("address"),
                    object.getDouble("lat"),
                    object.getDouble("lng"),
                    object.getString("_id"),
                    object.getInt("version"));
        }
        if(type.equals("topic")){
            return addTopic(content,
                    object.getString("name"),
                    object.getString("isAnon"),
                    object.getString("_id"),
                    object.getInt("version"));
        }
        if(type.equals("signup")){
            return addSignup(content,
                    object.getString("name"),
                    object.getString("dateInfo"),
                    object.getString("location"),
                    object.getString("address"),
                    object.getString("description"),
                    object.getInt("memberCount"),
                    object.getString("isMemberOf"),
                    object.getString("_id"),
                    object.getInt("version"));
        }
        //TODO Figure this out
//        if(type.equals("convo")){
//            JSONObject participant = object.getJSONObject("participant");
//            return addConvo(content,
//                    object.getString("pa"),
//                    object.getString("simpleFrom"),
//                    object.getString("simpleTo"),
//                    object.getString("subject"),
//                    object.getString("date"),
//                    participant.getString("user"),
//                    object.getString("message"),
//                    object.getInt("version")),
//                    object.getString("_id");
//        }
        if(type.equals("broadcast")){
            return addBC(content,
                    object.getString("title"),
                    object.getString("message"),
                    object.getInt("version"),
                    object.getString("_id"));
        }
        return null;
    }

    public ContentProviderOperation updateEvent(Uri existing, String title, String location, String date, String description, double lat, double lng, int version){
        return ContentProviderOperation.newUpdate(existing)
                .withValue(DataContract.Event.COLUMN_NAME_TITLE, title)
                .withValue(DataContract.Event.COLUMN_NAME_LOCATION, location)
                .withValue(DataContract.Event.COLUMN_NAME_DATE, date)
                .withValue(DataContract.Event.COLUMN_NAME_DESCRIPTION, description)
                .withValue(DataContract.Event.COLUMN_NAME_LAT, lat)
                .withValue(DataContract.Event.COLUMN_NAME_LNG, lng)
                .withValue(DataContract.Event.COLUMN_NAME_VERSION, version)
                .build();
    }

    public ContentProviderOperation addEvent(Uri existing, String title, String location, String date, String description, double lat, double lng, String id, int version){
        return ContentProviderOperation.newInsert(existing)
                .withValue(DataContract.Event.COLUMN_NAME_TITLE, title)
                .withValue(DataContract.Event.COLUMN_NAME_LOCATION, location)
                .withValue(DataContract.Event.COLUMN_NAME_DATE, date)
                .withValue(DataContract.Event.COLUMN_NAME_DESCRIPTION, description)
                .withValue(DataContract.Event.COLUMN_NAME_LAT, lat)
                .withValue(DataContract.Event.COLUMN_NAME_LNG, lng)
                .withValue(DataContract.Event.COLUMN_NAME_ENTRY_ID, id)
                .withValue(DataContract.Event.COLUMN_NAME_VERSION, version)
                .build();
    }

    public ContentProviderOperation updateTalk(Uri existing, String author, String subject, String date, String reference, String verse, JSONArray outline, int version) throws JSONException{
        String stringOutline = " ";
        for (int i = 0; i< outline.length(); i++){
            stringOutline += outline.getString(i);
            stringOutline += "\n";
        }
        return ContentProviderOperation.newUpdate(existing)
                .withValue(DataContract.Talk.COLUMN_NAME_AUTHOR, author)
                .withValue(DataContract.Talk.COLUMN_NAME_SUBJECT, subject)
                .withValue(DataContract.Talk.COLUMN_NAME_DATE, date)
                .withValue(DataContract.Talk.COLUMN_NAME_REFERENCE, reference)
                .withValue(DataContract.Talk.COLUMN_NAME_VERSE, verse)
                .withValue(DataContract.Talk.COLUMN_NAME_OUTLINE, stringOutline)
                .withValue(DataContract.Talk.COLUMN_NAME_VERSION, version)
                .build();
    }

    public ContentProviderOperation addTalk(Uri existing, String author, String subject, String date, String reference, String verse, JSONArray outline, String id, int version) throws JSONException{
        String stringOutline = " ";
        for (int i = 0; i< outline.length(); i++){
            stringOutline += outline.getString(i);
            stringOutline += "\n";
        }
        return ContentProviderOperation.newInsert(existing)
                .withValue(DataContract.Talk.COLUMN_NAME_AUTHOR, author)
                .withValue(DataContract.Talk.COLUMN_NAME_SUBJECT, subject)
                .withValue(DataContract.Talk.COLUMN_NAME_DATE, date)
                .withValue(DataContract.Talk.COLUMN_NAME_REFERENCE, reference)
                .withValue(DataContract.Talk.COLUMN_NAME_VERSE, verse)
                .withValue(DataContract.Talk.COLUMN_NAME_OUTLINE, stringOutline)
                .withValue(DataContract.Talk.COLUMN_NAME_ENTRY_ID, id)
                .withValue(DataContract.Talk.COLUMN_NAME_VERSION, version)
                .build();
    }

    public ContentProviderOperation addGroup(Uri existing, String name, String wTalks, String wSignups, String wEvents, String id, int version) throws JSONException{
        return ContentProviderOperation.newInsert(existing)
                .withValue(DataContract.Group.COLUMN_NAME_NAME, name)
                .withValue(DataContract.Group.COLUMN_NAME_WRITETALKS, wTalks)
                .withValue(DataContract.Group.COLUMN_NAME_WRITESIGNUPS, wSignups)
                .withValue(DataContract.Group.COLUMN_NAME_WRITEEVENTS, wEvents)
                .withValue(DataContract.Group.COLUMN_NAME_ENTRY_ID, id)
                .withValue(DataContract.Group.COLUMN_NAME_VERSION, version)
                .build();
    }

    public ContentProviderOperation updateGroup(Uri existing, String name, String wTalks, String wSignups, String wEvents, int version) throws JSONException{
        return ContentProviderOperation.newUpdate(existing)
                .withValue(DataContract.Group.COLUMN_NAME_NAME, name)
                .withValue(DataContract.Group.COLUMN_NAME_WRITETALKS, wTalks)
                .withValue(DataContract.Group.COLUMN_NAME_WRITESIGNUPS, wSignups)
                .withValue(DataContract.Group.COLUMN_NAME_WRITEEVENTS, wEvents)
                .withValue(DataContract.Group.COLUMN_NAME_VERSION, version)
                .build();
    }


    public ContentProviderOperation addLocation(Uri existing, String name, String address, double lat, double lng, String id, int version) throws JSONException{
        return ContentProviderOperation.newInsert(existing)
                .withValue(DataContract.Location.COLUMN_NAME_NAME, name)
                .withValue(DataContract.Location.COLUMN_NAME_ADDRESS, address)
                .withValue(DataContract.Location.COLUMN_NAME_LAT, lat)
                .withValue(DataContract.Location.COLUMN_NAME_LNG, lng)
                .withValue(DataContract.Location.COLUMN_NAME_ENTRY_ID, id)
                .withValue(DataContract.Location.COLUMN_NAME_VERSION, version)
                .build();
    }

    public ContentProviderOperation updateLocation(Uri existing, String name, String address, double lat, double lng, int version) throws JSONException{
        return ContentProviderOperation.newUpdate(existing)
                .withValue(DataContract.Location.COLUMN_NAME_NAME, name)
                .withValue(DataContract.Location.COLUMN_NAME_ADDRESS, address)
                .withValue(DataContract.Location.COLUMN_NAME_LAT, lat)
                .withValue(DataContract.Location.COLUMN_NAME_LNG, lng)
                .withValue(DataContract.Location.COLUMN_NAME_VERSION, version)
                .build();
    }

    public ContentProviderOperation updateTopic(Uri existing, String name, String isAnon, int version) throws JSONException{
        int anon = Boolean.parseBoolean(isAnon) ? 1 : 0;
        return ContentProviderOperation.newUpdate(existing)
                .withValue(DataContract.Topic.COLUMN_NAME_NAME, name)
                .withValue(DataContract.Topic.COLUMN_NAME_IS_ANON, anon)
                .withValue(DataContract.Topic.COLUMN_NAME_VERSION, version)
                .build();
    }

    public ContentProviderOperation updateSignup(Uri existing, String name, String dateInfo, String location, String address, String description, int memberCount, String memberOf, int version) throws JSONException{
        int member = Boolean.parseBoolean(memberOf) ? 1 : 0;
        return ContentProviderOperation.newUpdate(existing)
                .withValue(DataContract.Signup.COLUMN_NAME_NAME, name)
                .withValue(DataContract.Signup.COLUMN_NAME_DATE_INFO, dateInfo)
                .withValue(DataContract.Signup.COLUMN_NAME_LOCATION, location)
                .withValue(DataContract.Signup.COLUMN_NAME_ADDRESS, address)
                .withValue(DataContract.Signup.COLUMN_NAME_DESCRIPTION, description)
                .withValue(DataContract.Signup.COLUMN_NAME_MEMBER_COUNT, memberCount)
                .withValue(DataContract.Signup.COLUMN_NAME_MEMBER_OF, member)
                .withValue(DataContract.Signup.COLUMN_NAME_VERSION, version)
                .build();
    }

    public ContentProviderOperation addTopic(Uri existing, String name, String isAnon, String id, int version) throws JSONException{
        int anon = Boolean.parseBoolean(isAnon) ? 1 : 0;
        return ContentProviderOperation.newInsert(existing)
                .withValue(DataContract.Topic.COLUMN_NAME_NAME, name)
                .withValue(DataContract.Topic.COLUMN_NAME_IS_ANON, anon)
                .withValue(DataContract.Topic.COLUMN_NAME_ENTRY_ID, id)
                .withValue(DataContract.Topic.COLUMN_NAME_VERSION, version)
                .build();
    }

    public ContentProviderOperation addSignup(Uri existing, String name, String dateInfo, String location, String address, String description, int memberCount, String memberOf, String id, int version) throws JSONException{
        int member = Boolean.parseBoolean(memberOf) ? 1 : 0;
        return ContentProviderOperation.newInsert(existing)
                .withValue(DataContract.Signup.COLUMN_NAME_NAME, name)
                .withValue(DataContract.Signup.COLUMN_NAME_DATE_INFO, dateInfo)
                .withValue(DataContract.Signup.COLUMN_NAME_LOCATION, location)
                .withValue(DataContract.Signup.COLUMN_NAME_ADDRESS, address)
                .withValue(DataContract.Signup.COLUMN_NAME_DESCRIPTION, description)
                .withValue(DataContract.Signup.COLUMN_NAME_MEMBER_COUNT, memberCount)
                .withValue(DataContract.Signup.COLUMN_NAME_MEMBER_OF, member)
                .withValue(DataContract.Signup.COLUMN_NAME_ENTRY_ID, id)
                .withValue(DataContract.Signup.COLUMN_NAME_VERSION, version)
                .build();
    }

    public ContentProviderOperation addBC(Uri existing, String title, String message, int version, String id) throws JSONException{
        return ContentProviderOperation.newInsert(existing)
                .withValue(DataContract.Broadcast.COLUMN_NAME_TITLE, title)
                .withValue(DataContract.Broadcast.COLUMN_NAME_MSG, message)
                .withValue(DataContract.Broadcast.COLUMN_NAME_VERSION, version)
                .withValue(DataContract.Broadcast.COLUMN_NAME_ENTRY_ID, id)
                .build();
    }

    public ContentProviderOperation updateBC(Uri existing, String title, String message, int version) throws JSONException{
        return ContentProviderOperation.newUpdate(existing)
                .withValue(DataContract.Broadcast.COLUMN_NAME_TITLE, title)
                .withValue(DataContract.Broadcast.COLUMN_NAME_MSG, message)
                .withValue(DataContract.Broadcast.COLUMN_NAME_VERSION, version)
                .build();
    }
    //TODO add update/add convo

}
