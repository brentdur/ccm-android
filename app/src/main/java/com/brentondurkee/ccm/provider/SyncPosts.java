/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
 */

package com.brentondurkee.ccm.provider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;

import com.brentondurkee.ccm.Log;
import com.brentondurkee.ccm.Utils;
import com.brentondurkee.ccm.auth.AuthUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by brenton on 7/18/15.
 */
public class SyncPosts {

    public final static String EVENT_TITLE = "EVENT_TITLE";
    public final static String EVENT_DATE= "EVENT_DATE";
    public final static String EVENT_LOCATION= "EVENT_LOCATION";
    public final static String EVENT_ADDRESS = "EVENT_ADDRESS";
    public final static String EVENT_DESCRIPTION = "EVENT_DESCRIPTION";

    public final static String TALK_AUTHOR = "TALK_AUTHOR";
    public final static String TALK_SUBJECT = "TALK_SUBJECT";
    public final static String TALK_DATE = "TALK_DATE";
    public final static String TALK_REFERENCE = "TALK_REFERENCE";
    public final static String TALK_OUTLINE = "TALK_OUTLINE";

    public final static String MSG_TOPIC = "MSG_TOPIC";
    public final static String MSG_SUBJECT = "MSG_SUBJECT";
    public final static String MSG_MESSAGE = "MSG_MESSAGE";

    public final static String SIGNUP_NAME = "SIGNUP_NAME";
    public final static String SIGNUP_DATE_INFO = "SIGNUP_DATE_INFO";
    public final static String SIGNUP_LOCATION = "SIGNUP_LOCATION";
    public final static String SIGNUP_ADDRESS = "SIGNUP_ADDRESS";
    public final static String SIGNUP_DESCRIPTION = "SIGNUP_DESCRIPTION";

    public final static String PUT_USER_SIGNUP = "SIGNUP";

    public final static String DELETE_MESSAGE = "message";

    public final static String ME_SIGNUPS_KEY ="SIGNUPS";
    public final static String ME_EVENTS_KEY ="EVENTS";
    public final static String ME_TALKS_KEY ="TALKS";
    public final static String ME_MINISTERS_KEY ="MINISTERS";


    private final static String TAG = "SyncPosts";

    private final static String addEventUrl= Utils.DOMAIN + "/api/events";
    private final static String addTalkUrl=Utils.DOMAIN + "/api/talks";
    private final static String addMsgUrl=Utils.DOMAIN + "/api/messages";
    private final static String addSignupUrl=Utils.DOMAIN + "/api/signups";
    private final static String putUserToSignupUrl = Utils.DOMAIN + "/api/signups/addme";
    private final static String deleteMsgUrl = Utils.DOMAIN + "/api/messages";
    private final static String getMeUrl = Utils.DOMAIN + "/api/users/me";

    public static boolean addEvent(Bundle data, Account account, Context context){
        String token = AccountManager.get(context).peekAuthToken(account, AuthUtil.TOKEN_TYPE_ACCESS);
        Log.v(TAG, "Start Add Event");
        boolean good = false;
        try {
            URL url = new URL(addEventUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            PrintWriter output = new PrintWriter(out);
            String req = String.format("{\"title\": \"%s\",\"location\": \"%s\",\"date\": \"%s\",\"description\": \"%s\"", data.getString(EVENT_TITLE), data.getString(EVENT_LOCATION), data.getString(EVENT_DATE), data.getString(EVENT_DESCRIPTION).replace("\n","\\n"));
            if(!data.getString(EVENT_ADDRESS).isEmpty()){
                req += String.format(",\"address\":\"%s\"", data.getString(EVENT_ADDRESS));
            }
            req += "}";
            Log.v(TAG, req);
            output.print(req);
            output.close();

            int response = conn.getResponseCode();
            Log.v(TAG, "Response: " + response);
            if (response == 200) {
                good=true;
            }
            else {
                InputStream stream = new BufferedInputStream(conn.getInputStream());
                Scanner reader = new Scanner(stream);
                StringBuilder string = new StringBuilder();
                do {
                    string.append(reader.nextLine());
                } while(reader.hasNextLine());
                Log.d(TAG, "Raw Json: " + string.toString());
                reader.close();
            }
            conn.disconnect();
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
        }

        return good;
    }

    public static boolean addTalk(Bundle data, Account account, Context context){
        String token = AccountManager.get(context).peekAuthToken(account, AuthUtil.TOKEN_TYPE_ACCESS);
        Log.v(TAG, "Start Add Talk");
        boolean good = false;
        try {
            URL url = new URL(addTalkUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            PrintWriter output = new PrintWriter(out);
            String req = String.format("{\"author\": \"%s\",\"subject\": \"%s\",\"date\": \"%s\",\"reference\": \"%s\",\"outline\":[", data.getString(TALK_AUTHOR), data.getString(TALK_SUBJECT), data.getString(TALK_DATE), data.getString(TALK_REFERENCE));
            String[] outline = data.getStringArray(TALK_OUTLINE);
            for(int i = 0; i<outline.length; i++){
                req += String.format("\"%s\"" ,outline[i]);
                if(i+1 < outline.length){
                    req += ",";
                }
            }
            req += "]}";

            output.print(req);
            output.close();

            int response = conn.getResponseCode();
            Log.v(TAG, "Response: " + response);
            if (response == 200) {
                good=true;
            }
            else {
                InputStream stream = new BufferedInputStream(conn.getInputStream());
                Scanner reader = new Scanner(stream);
                StringBuilder string = new StringBuilder();
                do {
                    string.append(reader.nextLine());
                } while(reader.hasNextLine());
                Log.d(TAG, "Raw Json: " + string.toString());
                reader.close();
            }
            conn.disconnect();
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
        }

        return good;
    }

    public static boolean addMsg(Bundle data, Account account, Context context){
        String token = AccountManager.get(context).peekAuthToken(account, AuthUtil.TOKEN_TYPE_ACCESS);
        Log.v(TAG, "Start Add Message");
        boolean good = false;
        try {
            URL url = new URL(addMsgUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            PrintWriter output = new PrintWriter(out);
            String req = String.format("{\"topic\": \"%s\",\"subject\": \"%s\",\"message\":\"%s\"}", data.getString(MSG_TOPIC), data.getString(MSG_SUBJECT), data.getString(MSG_MESSAGE).replace("\n", "\\n"));

            output.print(req);
            output.close();

            int response = conn.getResponseCode();
            Log.v(TAG, "Response: " + response);
            if (response == 200) {
                good=true;
            }
            else {
                InputStream stream = new BufferedInputStream(conn.getInputStream());
                Scanner reader = new Scanner(stream);
                StringBuilder string = new StringBuilder();
                do {
                    string.append(reader.nextLine());
                } while(reader.hasNextLine());
                Log.d(TAG, "Raw Json: " + string.toString());
                reader.close();
            }
            conn.disconnect();
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
        }

        return good;
    }

    public static boolean addSignup(Bundle data, Account account, Context context){
        String token = AccountManager.get(context).peekAuthToken(account, AuthUtil.TOKEN_TYPE_ACCESS);
        Log.v(TAG, "Start Add Signup");
        boolean good = false;
        try {
            URL url = new URL(addSignupUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            PrintWriter output = new PrintWriter(out);
            String req = String.format("{\"name\": \"%s\",\"location\": \"%s\",\"dateInfo\": \"%s\",\"description\": \"%s\"", data.getString(SIGNUP_NAME), data.getString(SIGNUP_LOCATION), data.getString(SIGNUP_DATE_INFO), data.getString(SIGNUP_DESCRIPTION).replace("\n","\\n"));
            if(!data.getString(SIGNUP_ADDRESS).isEmpty()){
                req += String.format(",\"address\":\"%s\"", data.getString(SIGNUP_ADDRESS));
            }
            req += "}";
            Log.v(TAG, req);
            output.print(req);
            output.close();

            int response = conn.getResponseCode();
            Log.v(TAG, "Response: " + response);
            if (response == 200) {
                good=true;
            }
            else {
                InputStream stream = new BufferedInputStream(conn.getInputStream());
                Scanner reader = new Scanner(stream);
                StringBuilder string = new StringBuilder();
                do {
                    string.append(reader.nextLine());
                } while(reader.hasNextLine());
                Log.d(TAG, "Raw Json: " + string.toString());
                reader.close();
            }
            conn.disconnect();
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
        }

        return good;
    }

    public static boolean putUserToSignup(Bundle data, Account account, Context context){
        String token = AccountManager.get(context).peekAuthToken(account, AuthUtil.TOKEN_TYPE_ACCESS);
        Log.v(TAG, "Start Put user to Signup");
        boolean good = false;
        try {
            URL url = new URL(putUserToSignupUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            PrintWriter output = new PrintWriter(out);
            String req = String.format("{\"signup\": \"%s\"}", data.getString(PUT_USER_SIGNUP));
            Log.v(TAG, req);
            output.print(req);
            output.close();

            int response = conn.getResponseCode();
            Log.v(TAG, "Response: " + response);
            if (response == 200) {
                good=true;
            }
            else {
                InputStream stream = new BufferedInputStream(conn.getInputStream());
                Scanner reader = new Scanner(stream);
                StringBuilder string = new StringBuilder();
                do {
                    string.append(reader.nextLine());
                } while(reader.hasNextLine());
                Log.d(TAG, "Raw Json: " + string.toString());
                reader.close();
            }
            conn.disconnect();
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
        }

        return good;
    }

    public static boolean deleteMsg(Bundle data, Account account, Context context){
        String token = AccountManager.get(context).peekAuthToken(account, AuthUtil.TOKEN_TYPE_ACCESS);
        Log.v(TAG, "Start Delete Message");
        boolean good = false;
        try {
            URL url = new URL(deleteMsgUrl+"?message="+data.getString(DELETE_MESSAGE));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("DELETE");
            conn.setDoInput(true);
            conn.setChunkedStreamingMode(0);

            int response = conn.getResponseCode();
            Log.v(TAG, "Response: " + response);
            if (response == 200) {
                good=true;
            }
            else {
                InputStream stream = new BufferedInputStream(conn.getInputStream());
                Scanner reader = new Scanner(stream);
                StringBuilder string = new StringBuilder();
                do {
                    string.append(reader.nextLine());
                } while(reader.hasNextLine());
                Log.d(TAG, "Raw Json: " + string.toString());
                reader.close();
            }
            conn.disconnect();
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
        }

        return good;
    }

    public static Bundle getMe(Bundle data, Account account, Context context){
        Bundle retData = new Bundle();
        String token = AccountManager.get(context).peekAuthToken(account, AuthUtil.TOKEN_TYPE_ACCESS);
        Log.v(TAG, "Start Get Me");
        boolean good = false;
        try {
            URL url = new URL(getMeUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setChunkedStreamingMode(0);

            int response = conn.getResponseCode();
            Log.v(TAG, "Response: " + response);
            if (response == 200) {
                good=true;
                Scanner input = new Scanner(new BufferedInputStream(conn.getInputStream()));
                JSONObject retdata = new JSONObject(input.nextLine());
                JSONArray groups = retdata.getJSONArray("groups");
                for (int i = 0; i < groups.length(); i++){
                    JSONObject grp = groups.getJSONObject(i);
                    if(grp.getBoolean("writeSignups")){
                        retData.putBoolean(ME_SIGNUPS_KEY, true);
                    }
                    if(grp.getBoolean("writeEvents")){
                        retData.putBoolean(ME_EVENTS_KEY, true);
                    }
                    if(grp.getBoolean("writeTalks")){
                        retData.putBoolean(ME_TALKS_KEY, true);
                    }
                    if(grp.getString("name").equals("ministers")) {
                        retData.putBoolean(ME_MINISTERS_KEY, true);
                    }
                }
                Log.v(TAG, "Token: " + token);
            }
            else {
                InputStream stream = new BufferedInputStream(conn.getInputStream());
                Scanner reader = new Scanner(stream);
                StringBuilder string = new StringBuilder();
                do {
                    string.append(reader.nextLine());
                } while(reader.hasNextLine());
                Log.d(TAG, "Raw Json: " + string.toString());
                reader.close();
            }
            conn.disconnect();
        }
        catch(Exception e){
            Log.w(TAG, "Exception");
        }

        return retData;
    }

}
