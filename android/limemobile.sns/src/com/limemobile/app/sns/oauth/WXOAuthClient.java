package com.limemobile.app.sns.oauth;

import android.content.Context;
import android.widget.Toast;

import com.limemobile.app.sns.MetaDataUtils;
import com.limemobile.app.sns.SNSConstants;
import com.limemobile.app.sns.wxapi.WXUtil;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXOAuthClient {
    private int mSNSType;
    private IWXAPI wxApi;
    private Context mContext;

    public WXOAuthClient(Context appCtx) {
        mSNSType = SNSConstants.SNS_WECHAT;
        mContext = appCtx.getApplicationContext();
        wxApi = WXAPIFactory.createWXAPI(mContext,
                MetaDataUtils.getWechatAppID(mContext));
    }

    public boolean oauth(int wxNoInstallToastResource,
            int wxUnsupportOauthToastResource) {
        if(!WXUtil.isWXInstalled(mContext)) {
            if (wxNoInstallToastResource != 0) {
                Toast.makeText(mContext, wxNoInstallToastResource, Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        if(!WXUtil.isSupportOAuth(mContext)) {
            if (wxUnsupportOauthToastResource != 0) {
                Toast.makeText(mContext, wxUnsupportOauthToastResource, Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        wxApi.registerApp(MetaDataUtils.getWechatAppID(mContext));
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "weixin";
        if (!wxApi.sendReq(req)) {
            Toast.makeText(mContext, wxUnsupportOauthToastResource,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public int getSNSType() {
        return mSNSType;
    }
}
