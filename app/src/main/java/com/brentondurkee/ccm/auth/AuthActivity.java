package com.brentondurkee.ccm.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.brentondurkee.ccm.R;

/**
 * Created by brenton on 6/10/15.
 */
public class AuthActivity extends AccountAuthenticatorActivity{

    private final String TAG=getClass().getSimpleName();

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

        ((Button) findViewById(R.id.email_sign_in_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        ((Button) findViewById(R.id.email_register_button)).setOnClickListener(new View.OnClickListener() {
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
        }
    }

    public void submit(){
        Log.v(TAG, "Submit");
        final String email = ((TextView) findViewById(R.id.email)).getText().toString();
        final String password = ((TextView) findViewById(R.id.password)).getText().toString();
        final String accountType = getIntent().getStringExtra(AuthUtil.ARG_ACCOUNT_TYPE);

        new AsyncTask<Void, Void, Intent>(){
            @Override
            protected Intent doInBackground(Void... params){
                String authToken = AuthRequests.userSignIn(email, password, mAuthTokenType);
                final Intent res = new Intent();
                res.putExtra(AccountManager.KEY_ACCOUNT_NAME, email);
                res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                res.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
                res.putExtra(AuthUtil.PARAM_USER_PASS, password);
                return res;
            }
            @Override
            protected void onPostExecute(Intent intent){
                finishLogin(intent);
            }
        }.execute();
    }

    private void finishLogin(Intent intent){
        Log.v(TAG, "Finish Login");
        String email = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String password = intent.getStringExtra(AuthUtil.PARAM_USER_PASS);
        final Account account = new Account(email, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
        Log.v(TAG, "" + intent.toString());
        Log.v(TAG, "" + intent.getExtras().toString());
        if(getIntent().getBooleanExtra(AuthUtil.ARG_IS_ADDING_NEW, false)) {
            String authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authTokenType = mAuthTokenType;
            mAccountManager.addAccountExplicitly(account, password, null);
            mAccountManager.setAuthToken(account, authTokenType, authToken);
        } else {
            mAccountManager.setPassword(account, password);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }
}
