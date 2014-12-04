package com.limemobile.app.sns.oauth;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public abstract class OAuthClient {

    public static final String ACCESS_TOKEN_KEY = "access_token";
    public static final String UID_KEY = "uid";
    public static final String EXPIRES_IN_KEY = "expires_in";
    public static final String IS_AUTHORIZED = "is_authorized";
    public static final String ACCESS_TOKEN_VALID_TIME_KEY = "access_token_valid_time";

    protected Context mAppCtx;
    protected SharedPreferences mSP;
    protected String mOAuthInfoPath;
    protected OAuthCallback mOAuthCallback;
    protected boolean isAuthorized;
    protected int mSNSType;
    
    public OAuthClient(Context appCtx, String path) {
        if (appCtx == null) {
            throw new IllegalArgumentException();
        }
        mAppCtx = appCtx.getApplicationContext();
        mSP = appCtx.getSharedPreferences(path, Context.MODE_PRIVATE);
        isAuthorized = mSP.getBoolean(IS_AUTHORIZED, false);
    }

    public int getSNSType() {
        return mSNSType;
    }

    public abstract boolean oauth(Activity activity, OAuthCallback callback);

    // 判断用户是否已授权（本地）
    public boolean isAuthorized() {
        return isAuthorized;
    }

    // 获取用户已绑定账户对应的Access Token
    public String getAccessToken() {
        if (isAuthorized) {
            return mSP.getString(ACCESS_TOKEN_KEY, null);
        } else {
            return null;
        }
    }

    // 返回用户授权信息（本地）
    public String getUserId() {
        if (isAuthorized) {
            return mSP.getString(UID_KEY, null);
        } else {
            return null;
        }
    }

    // 返回用户授权过期时间
    public String getExpiredIn() {
        if (isAuthorized) {
            return mSP.getString(EXPIRES_IN_KEY, null);
        } else {
            return null;
        }
    }

    // 返回用户授权生效的时间
    public long getAccessTokenValidTime() {
        if (isAuthorized) {
            return mSP.getLong(ACCESS_TOKEN_VALID_TIME_KEY, System.currentTimeMillis());
        } else {
            return 0;
        }
    }

    public boolean presistOAuthInfo(String accessToken, String uid,
            String expired_in) {
        Editor mEditor = mSP.edit();
        mEditor.putString(ACCESS_TOKEN_KEY, accessToken);
        mEditor.putString(UID_KEY, uid);
        mEditor.putString(EXPIRES_IN_KEY, expired_in);
        mEditor.putBoolean(IS_AUTHORIZED, true);
        mEditor.putLong(ACCESS_TOKEN_VALID_TIME_KEY, System.currentTimeMillis());
        isAuthorized = true;
        return mEditor.commit();
    }

    // 注销账户
    public boolean writeOffAccount() {
        Editor mEditor = mSP.edit();
        mEditor.putString(ACCESS_TOKEN_KEY, null);
        mEditor.putString(UID_KEY, null);
        mEditor.putString(EXPIRES_IN_KEY, null);
        mEditor.putBoolean(IS_AUTHORIZED, false);
        mEditor.putLong(ACCESS_TOKEN_VALID_TIME_KEY, 0);
        isAuthorized = false;
        return mEditor.commit();
    }

    // 判断授权是否过期
    public abstract boolean isTokenValid();
}
