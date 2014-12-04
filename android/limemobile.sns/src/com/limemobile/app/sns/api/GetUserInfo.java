package com.limemobile.app.sns.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;

import com.limemobile.app.sns.SNSConstants;
import com.limemobile.app.sns.oauth.SinaOAuthClient;
import com.limemobile.app.sns.oauth.TencentOAuthClient;
import com.sina.weibo.sdk.android.api.UsersAPI;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

public class GetUserInfo {
    public static class SNSUserInfo {
        public final String nick;
        public final String sex;
        public final String picUrl;
        public final String birthday;

        public SNSUserInfo(String nick, String sex, String picUrl,
                String birthday) {
            this.nick = nick;
            this.sex = sex;
            this.picUrl = picUrl;
            this.birthday = birthday;
        }
    }

    public static interface GetUserInfoCallback {
        public void onGetUserInfoFinish(SNSUserInfo info);
    }

    public boolean execute(final Activity activity, final int snsType,
            final GetUserInfoCallback callback) {
        Context context = activity.getApplicationContext();
        switch (snsType) {
        case SNSConstants.SNS_QQ: {
            TencentOAuthClient client = new TencentOAuthClient(context);
            if (!client.isAuthorized()) {
                return false;
            }
            UserInfo userInfo = new UserInfo(context, client.mQQAuth.getQQToken());
            userInfo.getUserInfo(new TencentUiListener(activity, callback));
            return true;
        }
        case SNSConstants.SNS_WEIBO: {
            SinaOAuthClient client = new SinaOAuthClient(context);
            if (!client.isAuthorized()) {
                return false;
            }
            UsersAPI api = new UsersAPI(client.getOauth2AccessToken());
            Long uid = Long.valueOf(client.getUserId());
            api.show(uid, new RequestListener() {
                @Override
                public void onIOException(IOException arg0) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onGetUserInfoFinish(null);
                        }
                    });
                }

                @Override
                public void onError(WeiboException arg0) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onGetUserInfoFinish(null);
                        }
                    });
                }

                @Override
                public void onComplete(final String ret) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject result = new JSONObject(ret);
                                String nick = result.getString("screen_name");
                                if (nick != null) {
                                    nick.trim();
                                }
                                SNSUserInfo info = new SNSUserInfo(nick, null,
                                        null, null);
                                callback.onGetUserInfoFinish(info);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                callback.onGetUserInfoFinish(null);
                            }
                        }
                    });
                }

                @Override
                public void onComplete4binary(ByteArrayOutputStream arg0) {
                    // TODO Auto-generated method stub

                }
            });
            return true;
        }
        default:
            return false;
        }
    }

    private class TencentUiListener implements IUiListener {
        GetUserInfoCallback callback;
        Activity activity;

        public TencentUiListener(Activity activity, GetUserInfoCallback callback) {
            this.callback = callback;
            this.activity = activity;
        }

        @Override
        public void onComplete(final Object response) {
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        JSONObject json = (JSONObject) response;
                        SNSUserInfo info = new SNSUserInfo(json
                                .getString("nickname"), null, json
                                .getString("figureurl_qq_2"), null);
                        if (callback != null) {
                            callback.onGetUserInfoFinish(info);
                        }
                    } catch (JSONException e) {
                        if (callback != null) {
                            callback.onGetUserInfoFinish(null);
                        }
                    }
                }
            });

        }

        @Override
        public void onCancel() {
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onGetUserInfoFinish(null);
                    }
                }
            });
        }

        public void onError(UiError e) {
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onGetUserInfoFinish(null);
                    }
                }
            });
        }
    }

}
