package com.limemobile.app.sdk.http.volley;

import org.json.JSONObject;

import com.limemobile.app.sdk.http.BasicRequest;
import com.limemobile.app.sdk.http.JSONResponseListener;

public abstract class VolleyClientRequest extends BasicRequest {
    protected JSONObject mRequestParams;

    public VolleyClientRequest(String domain, String host, String path,
            JSONResponseListener listener) {
        super(domain, host, path, listener);

        mRequestParams = new JSONObject();
    }

    public void setRequestParams(JSONObject json) {
        mRequestParams = json;
    }

    public JSONObject getRequestParams() {
        return mRequestParams;
    }
}
