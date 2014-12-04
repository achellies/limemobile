package com.limemobile.app.sns.oauth;

import android.content.Intent;

public interface OnSsoAuthReturn {
	public void onSsoAuthReturn(int requestCode, int resultCode, Intent data);
}
