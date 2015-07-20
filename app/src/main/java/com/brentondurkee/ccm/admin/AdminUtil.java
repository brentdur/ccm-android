/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee.
 */

package com.brentondurkee.ccm.admin;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.brentondurkee.ccm.Pager;

/**
 * Created by brenton on 7/19/15.
 * Utility classes for admin activities
 */
public class AdminUtil {
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
}
