package com.limemobile.app.sns.share;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.limemobile.app.sns.SNSConstants;
import com.limemobile.app.sns.oauth.OAuthCallback;
import com.limemobile.app.sns.oauth.OAuthClient;
import com.limemobile.app.sns.oauth.TencentOAuthClient;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

public class QQZoneShareUtil {
    private QQZoneShareUtil() {
    }

    public static void share(final Activity activity,
            final ShareContent content, final ShareCallback callback,
            final TencentOAuthClient client) {
        if (client.isAuthorized() == false) {
            client.oauth(activity, new OAuthCallback() {
                @Override
                public void onComplete(Activity oauthActivity,
                        OAuthClient client) {
                    realShare(oauthActivity, content, callback,
                            (TencentOAuthClient) client);
                }

                @Override
                public void onCancel() {
                    callback.OnSentFailed(SNSConstants.RET_CODE_NOT_AUTHED,
                            null);
                }

                @Override
                public void onError(int errCode, String errMsg, Exception e) {
                    callback.OnSentFailed(SNSConstants.RET_CODE_NOT_AUTHED,
                            null);
                }

            });
        } else {
            realShare(activity, content, callback, client);
        }
    }

    private static void realShare(final Activity activity,
            final ShareContent content, final ShareCallback callback,
            final TencentOAuthClient client) {
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(content.title)) {
            bundle.putString(QzoneShare.SHARE_TO_QQ_TITLE, content.title);
        }
        if (!TextUtils.isEmpty(content.webUrl)) {
            bundle.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, content.webUrl);
        }
        if (!TextUtils.isEmpty(content.comment)) {
            bundle.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, content.comment);
        }
        if (!TextUtils.isEmpty(content.description)) {
            bundle.putString(QzoneShare.SHARE_TO_QQ_EXT_STR,
                    content.description);
        }
        if (!TextUtils.isEmpty(content.imageUrl)) {
            bundle.putString(QzoneShare.SHARE_TO_QQ_IMAGE_URL, content.imageUrl);
        }
        bundle.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        client.shareToQQZone(activity, bundle, new BaseUiListener(callback));
    }

    private static class BaseUiListener implements IUiListener {
        ShareCallback callback;

        public BaseUiListener(ShareCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onComplete(Object response) {
            try {
                JSONObject json = (JSONObject) response;
                int ret = json.getInt("ret");
                String retMsg = null;
                if (json.has("msg")) {
                    retMsg = json.getString("msg");
                }
                if (callback != null) {
                    callback.OnSentComplete(ret, retMsg);
                }
            } catch (JSONException e) {
                if (callback != null) {
                    callback.OnSentFailed(-1, e.getMessage());
                }
            } catch (ClassCastException e) {
                if (callback != null) {
                    callback.OnSentFailed(-1, e.getMessage());
                }
            }
        }

        @Override
        public void onError(UiError e) {
            if (callback != null) {
                callback.OnSentFailed(e.errorCode, e.errorDetail);
            }
        }

        @Override
        public void onCancel() {
            if (callback != null) {
                callback.OnSentFailed(-1, null);
            }
        }
    }
}
