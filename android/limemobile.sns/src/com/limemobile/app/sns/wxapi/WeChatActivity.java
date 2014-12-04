package com.limemobile.app.sns.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.limemobile.app.sns.MetaDataUtils;
import com.limemobile.app.sns.SNSConstants;
import com.limemobile.app.sns.oauth.WXOAuthCallback;
import com.limemobile.app.sns.share.ShareCallback;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * 此类为代码实例，需要用户继承此类并声明。 并且要将子类放到 xxx.wxapi包下 （xxx为应用程序包名），子类命名必须为
 * WXEntryActivity。
 * 
 * @author Administrator
 * 
 */
public abstract class WeChatActivity extends Activity implements
        IWXAPIEventHandler {
    protected static ShareCallback mShareCallback;
    protected static WXOAuthCallback mOAuthCallback;

    private IWXAPI mWXApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWXApi = WXAPIFactory.createWXAPI(this,
                MetaDataUtils.getWechatAppID(getApplicationContext()));
        mWXApi.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mWXApi.handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq req) {
        switch (req.getType()) {
        case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
            // goToGetMsg();
            break;
        case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
            // goToShowMsg((ShowMessageFromWX.Req) req);
            break;
        default:
            break;
        }
        finish();
    }

    @Override
    public void onResp(BaseResp resp) {
        int retCode = SNSConstants.RET_CODE_SUCCEED;
        switch (resp.errCode) {
        case BaseResp.ErrCode.ERR_OK:
            retCode = SNSConstants.RET_CODE_SUCCEED;
            break;
        case BaseResp.ErrCode.ERR_USER_CANCEL:
            retCode = SNSConstants.RET_CODE_USER_CANCEL;
            break;
        case BaseResp.ErrCode.ERR_AUTH_DENIED:
            retCode = SNSConstants.RET_CODE_AUTH_DENIED;
            break;
        default:
            retCode = SNSConstants.RET_CODE_UNKNOWN;
            break;
        }
        String result = getResultMessage(retCode);
        if (mShareCallback != null) {
            if (resp instanceof SendMessageToWX.Resp) {
                if (retCode == SNSConstants.RET_CODE_SUCCEED) {
                    mShareCallback.OnSentComplete(retCode, result);
                } else {
                    mShareCallback.OnSentFailed(retCode, null);
                }
            }
            mShareCallback = null;
        }
        if (mOAuthCallback != null) {
            if (resp instanceof SendAuth.Resp) {
                SendAuth.Resp resptemp = (SendAuth.Resp) resp;
                if (retCode == SNSConstants.RET_CODE_SUCCEED) {
                    mOAuthCallback.OnComplete(retCode, result, resptemp.code);
                } else {
                    mOAuthCallback.OnFailed(retCode, result);
                }
            }
            mOAuthCallback = null;
        }
        finish();
    }

    protected abstract String getResultMessage(int retCode);

    public static void setShareCallback(ShareCallback callback) {
        mShareCallback = callback;
    }

    public static void setOAuthCallback(WXOAuthCallback callback) {
        mOAuthCallback = callback;
    }

}
