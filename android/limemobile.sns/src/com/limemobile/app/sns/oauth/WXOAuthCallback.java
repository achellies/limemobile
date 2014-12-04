package com.limemobile.app.sns.oauth;

public interface WXOAuthCallback {
    void OnComplete(int retCode, String retMsg, String code);

    void OnFailed(int errorCode, String errMsg);
}
