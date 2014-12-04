package com.limemobile.app.sns.share;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.limemobile.app.sns.oauth.SinaOAuthClient;
import com.sina.weibo.sdk.api.share.IWeiboHandler;

/*
 * Usage:
 1. xxxActivity extent the abstract class
 2. Add this to xxxActivity (Refer to the 1 step is in the package name) task in manifest :
 <intent-filter>
 <action android:name="com.sina.weibo.sdk.action.ACTION_SDK_REQ_ACTIVITY" />
 <category android:name="android.intent.category.DEFAULT" />
 </intent-filter>
 3. complete the method of onResponse like this
 @Override
 public void onResponse(BaseResponse baseResp) {
 switch (baseResp.errCode) {
 case WBConstants.ErrorCode.ERR_OK:
 //to do sth
 break;
 case WBConstants.ErrorCode.ERR_CANCEL:
 ...
 break;
 case WBConstants.ErrorCode.ERR_FAIL:
 ...
 break;
 }
 }
 */
/**
 * 接收微博客户端分享后回调的acitivity
 */
public abstract class WeiboShareActivity extends Activity implements
        IWeiboHandler.Response {
    private static final int WEIBO_SHARE_REQUEST_CODE = 765;// 微博sdk2.4.0
                                                            // startActivityForResult中定义的requestCode
    protected SinaOAuthClient mSinaWeiboAuthClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSinaWeiboAuthClient = new SinaOAuthClient(this);
        if (savedInstanceState != null) {
            mSinaWeiboAuthClient.getWeiboShareAPI(this).handleWeiboResponse(
                    getIntent(), this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mSinaWeiboAuthClient.getWeiboShareAPI(this).handleWeiboResponse(intent,
                this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSinaWeiboAuthClient != null) {
            mSinaWeiboAuthClient.onSsoAuthReturn(requestCode, resultCode, data);
        }
        if (requestCode == WEIBO_SHARE_REQUEST_CODE) {
            if (resultCode == 0) {
                finish();
            }
        }
    }
}
