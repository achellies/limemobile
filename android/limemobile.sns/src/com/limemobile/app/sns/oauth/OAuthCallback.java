package com.limemobile.app.sns.oauth;

import android.app.Activity;


public interface OAuthCallback {
    public void onComplete(Activity oauthActivity, OAuthClient client);
    public void onCancel();
    public void onError(int errCode, String errMsg, Exception e);
}
