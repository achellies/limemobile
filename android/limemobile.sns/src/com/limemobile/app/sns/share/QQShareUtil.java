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
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

public class QQShareUtil {
    private QQShareUtil() {
    }

    public static void share(final Activity activity,
            final ShareContent content, final ShareCallback callback,
            final TencentOAuthClient client) {
        realShare(activity, content, callback, client);
    }

    public static void shareWithOauth(final Activity activity,
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
        client.shareToQQ(activity, prepareBundle(content), new BaseUiListener(
                callback));
    }

    private static Bundle prepareBundle(final ShareContent content) {
        Bundle bundle = null;
        bundle = new Bundle();

        if (TextUtils.isEmpty(content.audioUrl)) {
            bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE,
                    QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        } else {
            bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE,
                    QQShare.SHARE_TO_QQ_TYPE_AUDIO);
            bundle.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, content.audioUrl);
        }

        if (!TextUtils.isEmpty(content.title)) {
            bundle.putString(QQShare.SHARE_TO_QQ_TITLE, content.title);
        }
        if (!TextUtils.isEmpty(content.imageUrl)) {
            bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, content.imageUrl);
        }
        if (!TextUtils.isEmpty(content.webUrl)) {
            bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, content.webUrl);
        }
        if (!TextUtils.isEmpty(content.description)) {
            bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, content.description);
        }
        // bundle.putInt(QQShare.SHARE_TO_QQ_EXT_INT,
        // QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        return bundle;
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
