package com.limemobile.app.sns.share;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap.CompressFormat;

import com.limemobile.app.sns.SNSConstants;
import com.limemobile.app.sns.oauth.OAuthCallback;
import com.limemobile.app.sns.oauth.OAuthClient;
import com.limemobile.app.sns.oauth.SinaOAuthClient;
import com.sina.weibo.sdk.android.api.StatusesAPI;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.MusicObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.VideoObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.Utility;

public class WeiboShareUtil {

    private WeiboShareUtil() {
    }

    private static final int SUPPORT_MULTI_MESSAGE_MIN_API = 10351;
    private static final int MEDIA_DEFAULT_LENGTH = 10;

    public static boolean clientIsAvailable(SinaOAuthClient client,Context activty) {
        IWeiboShareAPI weiboShareAPI = client.getWeiboShareAPI(activty);
        return weiboShareAPI.isWeiboAppInstalled()
                && weiboShareAPI.checkEnvironment(false);
    }

    public static void share(final Activity activity,
            final ShareContent content, final ShareCallback callback,
            final SinaOAuthClient client, final boolean clientIsAvailable) {
        if (clientIsAvailable) {
            weiboClientShare(activity, content, callback, client);
        } else {
            weiboWebShare(activity, content, callback, client);
        }
    }

    private static void weiboWebShare(final Activity activity,
            final ShareContent content, final ShareCallback callback,
            final SinaOAuthClient client) {
        if (client.isAuthorized() == false
                || !client.getOauth2AccessToken().isSessionValid()) {
            client.oauth(activity, new OAuthCallback() {
                @Override
                public void onComplete(Activity oauthActivity,
                        OAuthClient client) {
                    apiRealShare(oauthActivity, content, callback,
                            (SinaOAuthClient) client);
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
            apiRealShare(activity, content, callback, client);
        }
    }

    private static void apiRealShare(final Activity activity,
            final ShareContent content, final ShareCallback callback,
            final SinaOAuthClient client) {
        Oauth2AccessToken token = client.getOauth2AccessToken();
        StatusesAPI api = new StatusesAPI(token);
        if (content.tweet != null && content.image == null) {
            api.update(content.tweet, "0", "0", new Callback(callback));
            return;
        }
        if (content.image != null && content.thumbImage != null) {
            ByteArrayOutputStream bos = null;
            FileOutputStream fos = null;
            String cacheFilePath = "";
            try {
                bos = new ByteArrayOutputStream();
                content.thumbImage.compress(CompressFormat.PNG, 100, bos);
                File cacheDir = activity.getCacheDir();
                File cacheImageFile = new File(cacheDir, "weibo.png");
                cacheImageFile.createNewFile();
                fos = new FileOutputStream(cacheImageFile);
                fos.write(bos.toByteArray());
                fos.flush();
                cacheFilePath = cacheImageFile.getAbsolutePath();
            } catch (IOException e) {
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                    if (bos != null) {
                        bos.close();
                    }
                } catch (IOException e) {
                }
            }
            final String path = cacheFilePath;
            api.upload(content.tweet, path, "0", "0", new Callback(callback) {

                @Override
                public void onComplete(String ret) {
                    super.onComplete(ret);
                    File file = new File(path);
                    if (file.isFile() && file.exists()) {
                        file.delete();
                    }
                }

                @Override
                public void onError(WeiboException e) {
                    super.onError(e);
                    File file = new File(path);
                    if (file.isFile() && file.exists()) {
                        file.delete();
                    }
                }

                @Override
                public void onIOException(IOException e) {
                    super.onIOException(e);
                    File file = new File(path);
                    if (file.isFile() && file.exists()) {
                        file.delete();
                    }
                }

            });
            return;
        }
        if (content.audioUrl != null) {
            api.update(content.audioUrl, "0", "0", new Callback(callback));
            return;
        }
        if (content.videoUrl != null) {
            api.update(content.videoUrl, "0", "0", new Callback(callback));
            return;
        }
    }

    private static void weiboClientShare(final Activity activity,
            final ShareContent content, final ShareCallback callback,
            final SinaOAuthClient client) {
        IWeiboShareAPI weiboShareAPI = client.getWeiboShareAPI(activity);
        weiboShareAPI.registerApp();
        if (weiboShareAPI.isWeiboAppInstalled()
                && weiboShareAPI.checkEnvironment(false)
                && weiboShareAPI.isWeiboAppSupportAPI()) {
            int supportApi = weiboShareAPI.getWeiboAppSupportAPI();
            boolean hasImage = content.image != null;
            boolean hasMusic = content.audioUrl != null;
            boolean hasVideo = content.videoUrl != null;
            boolean hasWebpage = content.webUrl != null;
            if (supportApi >= SUPPORT_MULTI_MESSAGE_MIN_API) {
                reqMultiMsg(client, content, activity, hasImage, hasMusic,
                        hasVideo, hasWebpage);
            } else {
                reqSingleMsg(client, content, activity, hasImage, hasMusic,
                        hasVideo, hasWebpage);
            }
        } else {
            callback.OnSentFailed(-1,
                    "Weibo Client isn't existed or it doesn't support SDK sharing.");
        }
    }

    static class Callback implements RequestListener {
        private ShareCallback mCB;

        public Callback(ShareCallback callback) {
            mCB = callback;
        }

        @Override
        public void onComplete(String ret) {
            mCB.OnSentComplete(SNSConstants.RET_CODE_SUCCEED, ret);
        }

        @Override
        public void onError(WeiboException e) {
            mCB.OnSentFailed(SNSConstants.RET_CODE_UNKNOWN, e.getMessage());
        }

        @Override
        public void onIOException(IOException e) {
            mCB.OnSentFailed(SNSConstants.RET_CODE_UNKNOWN, e.getMessage());
        }

        @Override
        public void onComplete4binary(ByteArrayOutputStream arg0) {
        }

    }

    private static void reqMultiMsg(SinaOAuthClient client,
            ShareContent content, Activity activity, boolean hasImage,
            boolean hasMusic, boolean hasVideo, boolean hasWebpage) {
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        if (hasImage) {
            weiboMessage.imageObject = getImageObj(content);
        }
        if (hasMusic) {
            weiboMessage.mediaObject = getMusicObj(content);
        }
        if (hasVideo) {
            weiboMessage.mediaObject = getVideoObj(content);
        }
        if (hasWebpage) {
            weiboMessage.mediaObject = getWebpageObj(content);
        }
        TextObject textObject = new TextObject();
        textObject.text = (content.title == null ? "" : content.title)
                + (content.description == null ? "" : content.description)
                + (content.tweet == null ? "" : content.tweet);
        weiboMessage.textObject = textObject;
        SendMultiMessageToWeiboRequest req = new SendMultiMessageToWeiboRequest();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.multiMessage = weiboMessage;
        client.getWeiboShareAPI(activity).sendRequest(req);
    }

    private static void reqSingleMsg(SinaOAuthClient client,
            ShareContent content, Activity activity, boolean hasImage,
            boolean hasMusic, boolean hasVideo, boolean hasWebpage) {
        WeiboMessage weiboMessage = new WeiboMessage();
        if (hasMusic) {
            weiboMessage.mediaObject = getMusicObj(content);
        } else if (hasVideo) {
            weiboMessage.mediaObject = getVideoObj(content);
        } else if (hasWebpage) {
            weiboMessage.mediaObject = getWebpageObj(content);
        }
        SendMessageToWeiboRequest req = new SendMessageToWeiboRequest();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = weiboMessage;
        client.getWeiboShareAPI(activity).sendRequest(req);
    }

    private static ImageObject getImageObj(ShareContent content) {
        ImageObject imageObject = new ImageObject();
        imageObject.setImageObject(content.image);
        return imageObject;
    }

    private static MusicObject getMusicObj(ShareContent content) {
        MusicObject musicObject = new MusicObject();
        musicObject.identify = Utility.generateGUID();
        musicObject.title = content.title;
        musicObject.description = content.description;
        if (content.thumbImage != null) {
            musicObject.setThumbImage(content.thumbImage);
        }
        musicObject.actionUrl = content.webUrl;
        musicObject.dataUrl = content.audioUrl;
        musicObject.dataHdUrl = content.audioUrl;
        musicObject.duration = MEDIA_DEFAULT_LENGTH;
        return musicObject;
    }

    private static VideoObject getVideoObj(ShareContent content) {
        VideoObject videoObject = new VideoObject();
        videoObject.identify = Utility.generateGUID();
        videoObject.title = content.title;
        videoObject.description = content.description;
        if (content.thumbImage != null) {
            videoObject.setThumbImage(content.thumbImage);
        }
        videoObject.actionUrl = content.videoUrl;
        videoObject.dataUrl = content.videoUrl;
        videoObject.dataHdUrl = content.videoUrl;
        videoObject.duration = MEDIA_DEFAULT_LENGTH;
        return videoObject;
    }

    private static WebpageObject getWebpageObj(ShareContent content) {
        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = content.title;
        mediaObject.description = content.description;
        mediaObject.actionUrl = content.webUrl;
        if (content.thumbImage != null) {
            mediaObject.setThumbImage(content.thumbImage);
        }
        return mediaObject;
    }
}
