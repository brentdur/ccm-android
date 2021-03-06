/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
 */

package com.brentondurkee.ccm.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.brentondurkee.ccm.Log;
import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.admin.AdminUtil;
import com.brentondurkee.ccm.provider.gcm.RegIntentService;

/**
 * Created by brenton on 6/10/15.
 * Activity for sign-in
 * Activity called when sign-in needed
 *
 * Relies on AuthUtil, SignupActivity, AuthRequests
 */
public class AuthActivity extends AccountAuthenticatorActivity{

    private final String TAG="AuthActivity";

    private final static int REQ_SIGNUP = 1;

    private AccountManager mAccountManager;
    private String mAuthTokenType;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.v(TAG, "Create Activity");

        mAccountManager = AccountManager.get(getBaseContext());

        String accountEmail = getIntent().getStringExtra(AuthUtil.ARG_ACCOUNT_EMAIL);
        mAuthTokenType = getIntent().getStringExtra(AuthUtil.ARG_AUTH_TYPE);

        if(mAuthTokenType == null)
            mAuthTokenType = AuthUtil.TOKEN_TYPE_ACCESS;

        if(accountEmail != null){
            ((TextView) findViewById(R.id.email)).setText(accountEmail);
        }

        findViewById(R.id.email_sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        findViewById(R.id.email_register_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signup = new Intent(getBaseContext(), SignUpActivity.class);
                signup.putExtras(getIntent().getExtras());
                startActivityForResult(signup, REQ_SIGNUP);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.v(TAG, "On Result");
        if(requestCode == REQ_SIGNUP && resultCode == RESULT_OK){
            finishLogin(data);
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
            Log.v(TAG, "Signup Failed");
            toast("Signup Failed");

        }
    }

    public void submit(){
        Log.v(TAG, "Submit");
        final String email = ((TextView) findViewById(R.id.email)).getText().toString();
        final String password = ((TextView) findViewById(R.id.password)).getText().toString();
        final String accountType = getIntent().getStringExtra(AuthUtil.ARG_ACCOUNT_TYPE);

        if (email.isEmpty() || password.isEmpty()){
            return;
        }
        AdminUtil.showDialog(this);

        new AsyncTask<Void, Void, Intent>(){
            @Override
            protected Intent doInBackground(Void... params){
                String authToken = AuthRequests.userSignIn(email, password);
                final Intent res = new Intent();
                res.putExtra(AccountManager.KEY_ACCOUNT_NAME, email);
                res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                res.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
                res.putExtra(AuthUtil.PARAM_USER_PASS, password);
                res.putExtra(AuthUtil.REG_TYPE, "Login");
                res.putExtra(AuthUtil.SUCCESS, 1);
                return res;
            }
            @Override
            protected void onPostExecute(Intent intent){
                AdminUtil.hideDialog();
                finishLogin(intent);
            }
        }.execute();
    }

    private void finishLogin(Intent intent){
        Log.v(TAG, "Finish Login");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString(RegIntentService.PREF_GCM_TOKEN, "").commit();

        String email = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String password = intent.getStringExtra(AuthUtil.PARAM_USER_PASS);
        String type = intent.getStringExtra(AuthUtil.REG_TYPE);
        final Account account = new Account(email, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
        Log.v(TAG, "" + intent.getExtras().toString());
        Log.v(TAG, intent.getStringExtra(AccountManager.KEY_AUTHTOKEN).contains("FAILED") + " return");
        if(intent.getStringExtra(AccountManager.KEY_AUTHTOKEN).contains("FAILED")){
            setResult(RESULT_CANCELED);
            finish();
            toast(type + " Failed");
        }
        else if(getIntent().getBooleanExtra(AuthUtil.ARG_IS_ADDING_NEW, false)) {
            toast(type + " Complete");
            String authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authTokenType = mAuthTokenType;
            mAccountManager.addAccountExplicitly(account, password, null);
            mAccountManager.setAuthToken(account, authTokenType, authToken);
        } else {
            toast(type + " Complete");
            mAccountManager.setPassword(account, password);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void toast(String message){
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
