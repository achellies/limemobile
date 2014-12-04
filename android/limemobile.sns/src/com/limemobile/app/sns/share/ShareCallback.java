package com.limemobile.app.sns.share;

public interface ShareCallback {
	void OnSentComplete(int retCode, String retMsg);
	void OnSentFailed(int errorCode, String errMsg);
}
