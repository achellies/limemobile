package com.limemobile.app.sns;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class MetaDataUtils {
    private static final String SNS_QQ_APP_ID_NAME = "SNS_QQ_APPID";
    private static final String SNS_QQ_APP_KEY_NAME = "SNS_QQ_APPKEY";
    private static final String SNS_WEIBO_APP_ID_NAME = "SNS_WEIBO_APPID";
    private static final String SNS_WEIBO_APP_KEY_NAME = "SNS_WEIBO_APPKEY";
    private static final String SNS_WECHAT_APP_ID_NAME = "SNS_WECHAT_APPID";
    private static final String SNS_WECHAT_APP_KEY_NAME = "SNS_WECHAT_APPKEY";

    public static String getQQAppID(Context ctx) {
        return getMetaDataValue(ctx, SNS_QQ_APP_ID_NAME);
    }

    public static String getWeiboAppID(Context ctx) {
        return getMetaDataValue(ctx, SNS_WEIBO_APP_ID_NAME);

    }

    public static String getWechatAppID(Context ctx) {
        return getMetaDataValue(ctx, SNS_WECHAT_APP_ID_NAME);
    }

    public static String getQQAppKey(Context ctx) {
        return getMetaDataValue(ctx, SNS_QQ_APP_KEY_NAME);
    }

    public static String getWeiboAppKey(Context ctx) {
        return getMetaDataValue(ctx, SNS_WEIBO_APP_KEY_NAME);
    }

    public static String getWechatAppKey(Context ctx) {
        return getMetaDataValue(ctx, SNS_WECHAT_APP_KEY_NAME);
    }

    public static String getMetaDataValue(Context ctx, String name) {
        if (ctx == null) {
            throw new IllegalArgumentException();
        }
        String value = null;
        PackageManager packageManager = ctx.getPackageManager();
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = packageManager.getApplicationInfo(
                    ctx.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo != null && applicationInfo.metaData != null) {
                value = applicationInfo.metaData.getString(name);
            }
        } catch (NameNotFoundException e) {
            return null;
        }
        if (value == null) {
            return null;
        } else {
            return value;
        }
    }
}
