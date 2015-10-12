/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee.
 */

package com.brentondurkee.ccm.admin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.brentondurkee.ccm.Pager;

/**
 * Created by brenton on 7/19/15.
 * Utility classes for admin activities
 */
public class AdminUtil {

    public final static String ADD_TYPE="ADD_TYPE";
    public final static String TYPE_EVENT="EVENT";
    public final static String TYPE_MSG="MSG";
    public final static String TYPE_TALK="TALK";
    public final static String TYPE_SIGNUP="SIGNUP";
    //TODO add convo bc

    private static ProgressDialog mDialog;



    /**
     *
     * @param context the context for the toast to display in
     * @param text the text for the toast to show
     */
    public static void toast(Context context, String text){
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Returns to the Pager Activity
     * @param context the context to start the new activity
     */
    public static void succeed(Context context){
        Intent intent = new Intent(context, Pager.class);
        context.startActivity(intent);
    }

    public static void showDialog(Context context){
        if(mDialog != null){
            hideDialog();
            return;
        }
        mDialog = new ProgressDialog(context);
        mDialog.setMessage("Please wait...");
        mDialog.setCancelable(false);
        mDialog.show();

    }

    public static void hideDialog(){
        mDialog.hide();
        mDialog = null;
    }
}
