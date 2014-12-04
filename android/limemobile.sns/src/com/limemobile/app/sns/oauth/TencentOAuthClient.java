package com.limemobile.app.sns.oauth;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.limemobile.app.sns.MetaDataUtils;
import com.limemobile.app.sns.SNSConstants;
import com.tencent.connect.auth.QQAuth;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

// http://wiki.connect.qq.com/
public class TencentOAuthClient extends OAuthClient implements OnSsoAuthReturn {

    private static final String TENCENT_QQ_OAUTH_INFO = "tencent_qq_oauth_info";

    private static final String SCOPE = "get_user_info,"
            + "get_simple_userinfo," + "get_user_profile," + "get_app_friends,"
            + "upload_photo," + "add_share," + "add_topic," + "list_album,"
            + "upload_pic," + "add_album," + "set_user_face," + "get_vip_info,"
            + "get_vip_rich_info," + "get_intimate_friends_weibo,"
            + "match_nick_tips_weibo";
    // private static final String SCOPE = "all";
    public final Tencent mTencent;
    public final QQAuth mQQAuth;

    public TencentOAuthClient(Context appCtx) {
        super(appCtx, TENCENT_QQ_OAUTH_INFO);
        mSNSType = SNSConstants.SNS_QQ;
        String appid = MetaDataUtils.getQQAppID(appCtx.getApplicationContext());
        mTencent = Tencent.createInstance(appid, appCtx);
        mQQAuth = QQAuth.createInstance(appid, appCtx);
    }

    public void shareToQQZone(Activity activity, Bundle params,
            IUiListener listener) {
        mTencent.shareToQzone(activity, params, listener);
    }

    public void shareToQQ(Activity activity, Bundle params, IUiListener listener) {
        mTencent.shareToQQ(activity, params, listener);
    }

    @Override
    public boolean oauth(final Activity activity, final OAuthCallback callback) {
        if (isAuthorized && isTokenValid()) {
            return false;
        }
        IUiListener listener = new IUiListener() {

            @Override
            public void onCancel() {
                if (callback != null) {
                    callback.onCancel();
                }
            }

            @Override
            public void onComplete(Object response) {
                try {
                    JSONObject json = (JSONObject) response;
                    int ret = 0;
                    String msg = null;
                    if (json.has("ret")) {
                        ret = json.getInt("ret");
                    }
                    if (json.has("msg")) {
                        msg = json.getString("msg");
                    }
                    if (ret == 0) {
                        String openId = json.getString("openid");
                        String accessToken = json.getString("access_token");
                        String expiresIn = json.getString("expires_in");
                        presistOAuthInfo(accessToken, openId, expiresIn);
                        if (callback != null) {
                            callback.onComplete(activity,
                                    TencentOAuthClient.this);
                        }
                    } else {
                        if (callback != null) {
                            callback.onError(ret, msg, null);
                        }
                    }
                } catch (JSONException e) {
                    if (callback != null) {
                        callback.onError(-1, e.getMessage(), e);
                    }
                } catch (ClassCastException e) {
                    if (callback != null) {
                        callback.onError(-1, e.getMessage(), e);
                    }
                }
            }

            @Override
            public void onError(UiError error) {
                if (callback != null) {
                    callback.onError(error.errorCode, error.errorMessage,
                            new Exception(error.errorDetail));
                }
            }
        };
        mQQAuth.setOpenId(activity, getUserId());
        mQQAuth.setAccessToken(getAccessToken(), getExpiredIn());
        mQQAuth.login(activity, SCOPE, listener);
        return false;
    }

    @Override
    public boolean isTokenValid() {
        try {
            String accessToken = getAccessToken();
            long expiresTime = getAccessTokenValidTime()
                    + Long.parseLong(getExpiredIn()) * 1000;
            return (!TextUtils.isEmpty(accessToken) && (expiresTime == 0 || (System
                    .currentTimeMillis() < expiresTime)));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void onSsoAuthReturn(int requestCode, int resultCode, Intent data) {
        mTencent.onActivityResult(requestCode, resultCode, data);
    }
}
