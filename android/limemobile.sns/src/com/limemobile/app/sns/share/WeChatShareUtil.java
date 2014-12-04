package com.limemobile.app.sns.share;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.text.TextUtils;
import android.widget.Toast;

import com.limemobile.app.sns.MetaDataUtils;
import com.limemobile.app.sns.share.ShareContent.Builder;
import com.limemobile.app.sns.wxapi.WXUtil;
import com.limemobile.app.sns.wxapi.WeChatActivity;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage.IMediaObject;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WeChatShareUtil {

    private WeChatShareUtil() {
    }

    public static boolean share(final Activity activity,
            final ShareContent content, final ShareCallback callback,
            boolean isTimeLine, boolean isMusic,
            int wechatNoInstallToastResource) {
        IWXAPI api = WXAPIFactory.createWXAPI(activity.getApplicationContext(),
                MetaDataUtils.getWechatAppID(activity.getApplicationContext()),
                false);
        if (!WXUtil.isSupportWX(activity.getApplicationContext())) {
            if (wechatNoInstallToastResource != 0) {
                Toast.makeText(activity, wechatNoInstallToastResource,
                        Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        if (isTimeLine
                && !WXUtil.isSupportTimeLine(activity.getApplicationContext())) {
            if (wechatNoInstallToastResource != 0) {
                Toast.makeText(activity, wechatNoInstallToastResource,
                        Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        api.registerApp(MetaDataUtils.getWechatAppID(activity
                .getApplicationContext()));
        IMediaObject mMediaObject = null;
        if (isMusic) {
            WXMusicObject localWXMusicObject = new WXMusicObject();
            if (!TextUtils.isEmpty(content.audioUrl)) {
                localWXMusicObject.musicDataUrl = content.audioUrl;
            }
            if (!TextUtils.isEmpty(content.webUrl)) {
                localWXMusicObject.musicUrl = content.webUrl;
            }
            mMediaObject = localWXMusicObject;
        } else {
            if (!TextUtils.isEmpty(content.webUrl)) {
                WXWebpageObject localWXWebpageObject = new WXWebpageObject();
                localWXWebpageObject.webpageUrl = content.webUrl;
                mMediaObject = localWXWebpageObject;
            } else {
                WXTextObject localWXTextObject = new WXTextObject();
                localWXTextObject.text = content.tweet;
                mMediaObject = localWXTextObject;
            }
        }
        WXMediaMessage localWXMediaMessage = new WXMediaMessage(mMediaObject);
        if (!TextUtils.isEmpty(content.title)) {
            localWXMediaMessage.title = content.title;
        }

        if (!TextUtils.isEmpty(content.description)) {
            localWXMediaMessage.description = content.description;
        }

        if (content.image != null && content.thumbImage != null) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            content.thumbImage.compress(CompressFormat.PNG, 100, output);
            localWXMediaMessage.thumbData = output.toByteArray();
            try {
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = localWXMediaMessage;
        req.scene = isTimeLine ? SendMessageToWX.Req.WXSceneTimeline
                : SendMessageToWX.Req.WXSceneSession;
        WeChatActivity.setShareCallback(callback);
        api.sendReq(req);
        return true;
    }

    /**
     * @param webUrl
     *            必须。分享所在网页资源的链接，点击后跳转至第三方网页， 请以http://开头。
     * @param title
     *            必须。feeds的标题，最长36个中文字，超出部分会被截断。
     * @param description
     *            所分享的网页资源的摘要内容，或者是网页的概要描述。 最长80个中文字，超出部分会被截断。
     * @param image
     *            需要分享的图片bitmap,set之后会自己生成对应的thumbImage
     * @return
     */
    public static ShareContent getWebPageShareContent(String webUrl,
            String title, String description, Bitmap image) {
        Builder mBuilder = new Builder();
        mBuilder.setWebUrl(webUrl);
        mBuilder.setTitle(title);
        mBuilder.setDescription(description);
        mBuilder.setBitmap(image);
        return mBuilder.build();
    }

    /**
     * 获取分享音乐类型的content
     * 
     * @param musicUrl
     *            音乐的播放地址
     * @param title
     *            分享的标题
     * @param description
     *            分享的描述
     * @param image
     *            分享的头像，不能为空，否则分享后不能显示。
     * @return ShareContent
     */
    public static final ShareContent getMusicShareContent(String musicUrl,
            String musicWebUrl, String title, String description, Bitmap image) {
        ShareContent content = new ShareContent.Builder().setAudioUrl(musicUrl)
                .setWebUrl(musicWebUrl).setTitle(title)
                .setDescription(description).setBitmap(image).build();
        return content;
    }

    public static final ShareContent getTextShareContent(String title,
            String description, String tweet) {
        ShareContent content = new ShareContent.Builder().setTitle(title)
                .setDescription(description).setTweet(tweet).build();
        return content;
    }
}
