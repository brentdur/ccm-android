package com.brentondurkee.ccm.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by brenton on 6/10/15.
 */
public class Authenticator extends AbstractAccountAuthenticator {

    Context mContext;
    private final String TAG=getClass().getSimpleName();

    public Authenticator(Context context){
        super(context);
        mContext = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.v(TAG, "Add Account");
        final Intent intent = new Intent(mContext, AuthActivity.class);
        intent.putExtra(AuthUtil.ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(AuthUtil.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(AuthUtil.ARG_IS_ADDING_NEW, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;

    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.v(TAG, "Get Token");
        final AccountManager am = AccountManager.get(mContext);
        String authToken = am.peekAuthToken(account, authTokenType);

        if(TextUtils.isEmpty(authToken)){
            final String password = am.getPassword(account);
            if (password != null){
                authToken = AuthRequests.userSignIn(account.name, password, authTokenType);
            }
        }
        else {
            // TODO: add "test authKey"
        }

        if(!TextUtils.isEmpty(authToken)){
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        final Intent intent = new Intent(mContext, AuthActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(AuthUtil.ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(AuthUtil.ARG_AUTH_TYPE, authTokenType);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }
}
