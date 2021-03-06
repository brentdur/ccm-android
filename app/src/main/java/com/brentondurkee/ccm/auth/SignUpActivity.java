/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee.
 */

package com.brentondurkee.ccm.auth;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.brentondurkee.ccm.Log;
import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.admin.AdminUtil;

/**
 * Created by brenton on 6/10/15.
 * Activity used when a new account needs to be registerd
 *
 * Relies on AuthRequets and AuthUtil
 */
public class SignUpActivity extends Activity {

    private String mAccountType;
    private boolean fine = true;

    private final String TAG=getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "Create Activity");

        mAccountType = getIntent().getStringExtra(AuthUtil.ARG_ACCOUNT_TYPE);

        setContentView(R.layout.activity_signup);

        findViewById(R.id.signup_exists).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        findViewById(R.id.signup_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

    }

    public void createAccount(){
        final String name = ((EditText)findViewById(R.id.signup_name)).getText().toString();
        final String email = ((EditText)findViewById(R.id.signup_email)).getText().toString();
        final String password = ((EditText)findViewById(R.id.signup_password)).getText().toString();

        if(name.isEmpty() || email.isEmpty() || password.isEmpty()){
            return;
        }

        Log.v(TAG, "Create Account");
        AdminUtil.showDialog(this);
        new AsyncTask<String, Void, Intent>() {



            @Override
            protected Intent doInBackground(String... params) {
                String authToken = null;
                Bundle data = new Bundle();
                try{
                    authToken = AuthRequests.userSignUp(name, email, password);
                    if(authToken.contains("FAILED")){
                        fine = false;
                    }
                    data.putString(AccountManager.KEY_ACCOUNT_NAME, email);
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, mAccountType);
                    data.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                    data.putString(AuthUtil.PARAM_USER_PASS, password);
                    data.putString(AuthUtil.REG_TYPE, "Signup");
                    data.putInt(AuthUtil.SUCCESS, 1);
                } catch(Exception e){
                    Log.w(TAG, e.toString());
                }

                final Intent res = new Intent();
                res.putExtras(data);
                return res;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                AdminUtil.hideDialog();
                if(fine) {
                    setResult(RESULT_OK, intent);
                }
                else {
                    setResult(RESULT_CANCELED);
                }
                finish();
            }
        }.execute();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
