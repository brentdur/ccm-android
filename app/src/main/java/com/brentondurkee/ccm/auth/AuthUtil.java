package com.brentondurkee.ccm.auth;

/**
 * Created by brenton on 6/10/15.
 */
public class AuthUtil {
    //keys for incoming intent
    public final static String ARG_ACCOUNT_TYPE="ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE="AUTH_TYPE";
    public final static String ARG_ACCOUNT_EMAIL="AcCOUNT_EMAIL";
    public final static String ARG_IS_ADDING_NEW="IS_ADDING_ACCOUNT";

    //key for return intent
    public final static String PARAM_USER_PASS="USER_PASS";

    //type of token
    public final static String TOKEN_TYPE_ACCESS="user";

    public final static String ACCOUNT_TYPE="com.brentondurkee.ccm";


    public final static String REG_TYPE="REG_TYPE";
    public final static String SUCCESS="SUCCESS";

    //return intent
//    res.putExtra(AccountManager.KEY_ACCOUNT_NAME, email);
//    res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
//    res.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
//    res.putExtra(AuthUtil.PARAM_USER_PASS, password);
}
