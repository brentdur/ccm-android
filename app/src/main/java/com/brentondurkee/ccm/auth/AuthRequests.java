/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
 */

package com.brentondurkee.ccm.auth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.brentondurkee.ccm.Log;

/**
 * Created by brenton on 6/10/15.
 * Requests for authentication and user purposes
 * No Reliance
 */
public class AuthRequests {

    private final static String TAG = "AuthRequests";

    private final static String signUpUrl="http://ccm.brentondurkee.com/api/users";
    private final static String signInUrl="http://ccm.brentondurkee.com/auth/local";
    private final static String GCMUrl="http://ccm.brentondurkee.com/api/users/gcm";

    public static String userSignUp(final String name, final String email, final String pass){
        Log.v(TAG, "Start Signup");
        String token = "";
        try {
            URL url = new URL(signUpUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                //allow input and output
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setChunkedStreamingMode(0);

                PrintWriter output = new PrintWriter(new BufferedOutputStream(conn.getOutputStream()));
                //send input string
                String req = String.format("name=%s&email=%s&password=%s", name, email, pass);
                output.print(req);
                output.close();

                int response = conn.getResponseCode();
                Log.v(TAG, "Response: " + response);
                if (response == 200) {
                    Scanner input = new Scanner(new BufferedInputStream(conn.getInputStream()));
                    JSONObject data = new JSONObject(input.nextLine());
                    token = data.getString("token");
                    Log.v(TAG, "Token: " + token);
                }
                else {
                    Log.v(TAG, "SignUp Failed: " + response);
                    token = "FAILED: " + response;
                }
            }
            catch(JSONException e){
                Log.w(TAG, "JSON Exception");
                token = "FAILED";
            }
            finally {
                conn.disconnect();
            }
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
            token = "FAILED";
        }

        return token;
    }

    public static boolean updateGCM(final String gcm, final String token){
        Log.v(TAG, "Start GCM");
        boolean good = false;
        try {
            URL url = new URL(GCMUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Authorization", "Bearer " + token);
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);

            PrintWriter output = new PrintWriter(new BufferedOutputStream(conn.getOutputStream()));
            String req = String.format("gcm=%s", gcm);
            output.print(req);
            output.close();

            int response = conn.getResponseCode();
            Log.v(TAG, "Response: " + response);
            if (response == 200) {
                good=true;
             }
            conn.disconnect();
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
        }

        return good;
    }

    public static boolean testAuth(final String auth){
        //TODO: fill with test to call to an auth-requried end point
        //true means it's valid
        return true;
    }


    public static String userSignIn(final String email, final String pass){
        Log.v(TAG, "Start Signin");
        String token = "";
        try {
            URL url = new URL(signInUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setChunkedStreamingMode(0);

                PrintWriter output = new PrintWriter(new BufferedOutputStream(conn.getOutputStream()));
                String req = String.format("email=%s&password=%s", email, pass);
                output.print(req);
                output.close();

                int response = conn.getResponseCode();
                Log.v(TAG, "Response: " + response);
                if (response == 200) {
                    Scanner input = new Scanner(new BufferedInputStream(conn.getInputStream()));
                    JSONObject data = new JSONObject(input.nextLine());
                    token = data.getString("token");
                    Log.v(TAG, "Token: " + token);
                }
                else {
                    Log.v(TAG, "SignUp Failed: " + response);
                    token = "FAILED: " + response;
                }
            }
            catch(JSONException e){
                Log.w(TAG, "JSON Exception");
                token = "FAILED";
            }
            finally {
                conn.disconnect();
            }
        }
        catch(IOException e){
            Log.w(TAG, "IOException");
            token = "FAILED";
        }

        return token;
    }
}
