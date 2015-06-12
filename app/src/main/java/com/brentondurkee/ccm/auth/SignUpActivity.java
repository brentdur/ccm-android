package com.brentondurkee.ccm.auth;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.brentondurkee.ccm.R;

/**
 * Created by brenton on 6/10/15.
 */
public class SignUpActivity extends Activity {

    private String mAccountType;

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
        Log.v(TAG, "Create Account");
        new AsyncTask<String, Void, Intent>() {
            String name = ((EditText)findViewById(R.id.signup_name)).getText().toString();
            String email = ((EditText)findViewById(R.id.signup_email)).getText().toString();
            String password = ((EditText)findViewById(R.id.signup_password)).getText().toString();

            @Override
            protected Intent doInBackground(String... params) {
                String authToken = null;
                Bundle data = new Bundle();
                try{
                    authToken = AuthRequests.userSignUp(name, email, password, AuthUtil.TOKEN_TYPE_ACCESS);
                    //TODO: what if signup fails
                    data.putString(AccountManager.KEY_ACCOUNT_NAME, email);
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, mAccountType);
                    data.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                    data.putString(AuthUtil.PARAM_USER_PASS, password);
                } catch(Exception e){

                }

                final Intent res = new Intent();
                res.putExtras(data);
                return res;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                setResult(RESULT_OK, intent);
                //TODO: update user
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
