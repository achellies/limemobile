package com.limemobile.app.sns.wxapi;

import android.content.Context;

import com.limemobile.app.sns.MetaDataUtils;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXUtil {
    public static final boolean isWXInstalled(Context ctx) {
        IWXAPI api = WXAPIFactory.createWXAPI(ctx,
                MetaDataUtils.getWechatAppID(ctx), false);
        return api.isWXAppInstalled();
    }

    public static final boolean isSupportWX(Context ctx) {
        IWXAPI api = WXAPIFactory.createWXAPI(ctx,
                MetaDataUtils.getWechatAppID(ctx), false);
        return api.isWXAppInstalled() && api.isWXAppSupportAPI();
    }

    public static final boolean isSupportTimeLine(Context ctx) {
        IWXAPI api = WXAPIFactory.createWXAPI(ctx,
                MetaDataUtils.getWechatAppID(ctx), false);
        if (api.isWXAppInstalled() && api.isWXAppSupportAPI()) {
            int wxSdkVersion = api.getWXAppSupportAPI();
            if (wxSdkVersion >= Build.TIMELINE_SUPPORTED_SDK_INT) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static final boolean isSupportOAuth(Context ctx) {
        IWXAPI api = WXAPIFactory.createWXAPI(ctx,
                MetaDataUtils.getWechatAppID(ctx), false);
        if (api.isWXAppInstalled() && api.isWXAppSupportAPI()) {
            int wxSdkVersion = api.getWXAppSupportAPI();
            if (wxSdkVersion >= Build.OPENID_SUPPORTED_SDK_INT) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
