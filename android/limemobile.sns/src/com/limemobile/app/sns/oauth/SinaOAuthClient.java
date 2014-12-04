package com.limemobile.app.sns.oauth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.limemobile.app.sns.MetaDataUtils;
import com.limemobile.app.sns.SNSConstants;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

public class SinaOAuthClient extends OAuthClient implements OnSsoAuthReturn {

    private static final String SINA_WEIBO_OAUTH_INFO = "sina_weibo_oauth_info";
    private static final String REDIRECT_URL = "http://www.sina.com";
    private static final String SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog, invitation_write";
    private SsoHandler mSsoHandler;
    private IWeiboShareAPI mWeiboShareAPI;

    public SinaOAuthClient(Context appCtx) {
        super(appCtx, SINA_WEIBO_OAUTH_INFO);
        mAppCtx = appCtx;
        mSNSType = SNSConstants.SNS_WEIBO;
    }

    public synchronized IWeiboShareAPI getWeiboShareAPI(Context activity) {
        if (mWeiboShareAPI == null) {
            mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(activity,
                    MetaDataUtils.getWeiboAppID(mAppCtx));
        }
        return mWeiboShareAPI;
    }

    @Override
    public boolean oauth(Activity activity, OAuthCallback callback) {
        if (isAuthorized) {
            return false;
        }
        mOAuthCallback = callback;
        WeiboAuth weiboAuth = new WeiboAuth(activity,
                MetaDataUtils.getWeiboAppID(activity.getApplicationContext()),
                REDIRECT_URL, SCOPE);
        mSsoHandler = new SsoHandler(activity, weiboAuth);
        mSsoHandler.authorize(new AuthDialogListener(activity, callback, this));
        return true;
    }

    @Override
    public boolean isTokenValid() {
        Oauth2AccessToken token = new Oauth2AccessToken(getAccessToken(),
                getExpiredIn());
        return token.isSessionValid();
    }

    public Oauth2AccessToken getOauth2AccessToken() {
        return new Oauth2AccessToken(getAccessToken(), getExpiredIn());
    }

    class AuthDialogListener implements WeiboAuthListener {
        private OAuthCallback mCallback;
        private SinaOAuthClient mClient;
        private Activity mActivity;

        public AuthDialogListener(Activity activity, OAuthCallback callback,
                SinaOAuthClient sinaOAuthClient) {
            mCallback = callback;
            mClient = sinaOAuthClient;
            mActivity = activity;
        }

        @Override
        public void onComplete(Bundle values) {
            Oauth2AccessToken mAccessToken = Oauth2AccessToken
                    .parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                String token = values.getString("access_token");
                String expires_in = values.getString("expires_in");
                String openid = values.getString("uid");
                mClient.presistOAuthInfo(token, openid, expires_in);
                mCallback.onComplete(mActivity, mClient);
            } else {
                String code = values.getString("code");
                if (code == null) {
                    code = "-1";
                }
                try {
                    mCallback.onError(Integer.valueOf(code),
                            "session is not valid", null);
                } catch (Exception e) {
                    mCallback.onError(0, "session is not valid", null);
                }
            }
        }

        @Override
        public void onCancel() {
            mCallback.onCancel();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            mCallback.onError(0, e.getMessage(), e);
        }
    }

    @Override
    public void onSsoAuthReturn(int requestCode, int resultCode, Intent data) {
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }
}
